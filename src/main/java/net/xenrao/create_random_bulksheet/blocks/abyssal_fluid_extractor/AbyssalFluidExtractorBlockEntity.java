package net.xenrao.create_random_bulksheet.blocks.abyssal_fluid_extractor;

import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.math.BlockFace;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.xenrao.create_random_bulksheet.blocks.RandomBulkSheetBlockEntities;
import net.xenrao.create_random_bulksheet.RandomBulkSheetConfig;
import net.xenrao.create_random_bulksheet.items.RandomBulkSheetItems;
import net.xenrao.create_random_bulksheet.recipe.RandomBulkSheetRecipes;
import net.xenrao.create_random_bulksheet.recipe.fluid_extracting.FluidExtractingRecipe;

import java.util.*;

public class AbyssalFluidExtractorBlockEntity extends KineticBlockEntity {

    private static final Direction PUSH_SIDE = Direction.DOWN;

    protected boolean hasVoidStar;
    protected float fluidAmount;
    protected Fluid bufferedFluid = Fluids.EMPTY;

    private final IFluidHandler fluidHandler = new IFluidHandler() {

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            if (bufferedFluid == Fluids.EMPTY || fluidAmount <= 0)
                return FluidStack.EMPTY;
            return new FluidStack(bufferedFluid, (int) fluidAmount);
        }

        @Override
        public int getTankCapacity(int tank) {
            return (int) RandomBulkSheetConfig.EXTRACTOR_MAX_BUFFER_MB.get().doubleValue();
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return drainInternal(resource.getAmount(), action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return drainInternal(maxDrain, action);
        }
    };

    private FluidStack drainInternal(int maxDrain, IFluidHandler.FluidAction action) {
        if (bufferedFluid == Fluids.EMPTY || fluidAmount <= 0)
            return FluidStack.EMPTY;

        int toDrain = Math.min(maxDrain, (int) fluidAmount);
        if (toDrain <= 0)
            return FluidStack.EMPTY;

        Fluid drained = bufferedFluid;
        if (action.execute()) {
            fluidAmount -= toDrain;
            if (fluidAmount <= 0) {
                fluidAmount = 0;
                bufferedFluid = Fluids.EMPTY;
            }
            setChanged();
        }
        return new FluidStack(drained, toDrain);
    }

    public AbyssalFluidExtractorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(20);
    }

    public ItemInteractionResult addGems(Player player, ItemStack item) {
        if (item.is(RandomBulkSheetItems.VOID_STAR.get()) && !hasVoidStar) {
            hasVoidStar = true;
            setChanged();
            sendData();
            if (!player.isCreative()) item.shrink(1);
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                RandomBulkSheetBlockEntities.ABYSSAL_FLUID_EXTRACTOR.get(),
                (be, side) -> {
                    if (side == PUSH_SIDE)
                        return be.fluidHandler;
                    return null;
                }
        );
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        behaviours.add(new ExtractorPumpBehaviour(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide)
            return;
        accumulateFluid();
    }

    private float getMaxBuffer() {
        return RandomBulkSheetConfig.EXTRACTOR_MAX_BUFFER_MB.get().floatValue();
    }

    private void accumulateFluid() {
        float rpm = Math.abs(getSpeed());
        if (rpm <= 0) {
            /*
            if (fluidAmount < 1) {
                fluidAmount = 0;
                bufferedFluid = Fluids.EMPTY;
                setChanged();
            }

             */
            return;
        }
        FluidState aboveState = level.getFluidState(worldPosition.above());
        if (aboveState.isEmpty())
            return;

        Fluid aboveFluid = aboveState.getType();

        if (bufferedFluid != Fluids.EMPTY && bufferedFluid != aboveFluid) {
            // Farklı sıvı geliyor. Eski buffer'da 1mB'nin altında,
            // asla aktarılamayacak bir kalıntı varsa temizle ki kilitlenmesin.
            if (fluidAmount < 1f) {
                fluidAmount = 0;
                bufferedFluid = Fluids.EMPTY;
            } else {
                return; // hâlâ aktarılabilir miktar var, bekle
            }
        }

        double maxBuffer = RandomBulkSheetConfig.EXTRACTOR_MAX_BUFFER_MB.get();
        if (fluidAmount >= maxBuffer)
            return;

        FluidExtractingRecipe recipe = findRecipeFor(aboveFluid);

        boolean requiresStar;
        float ratePerRpm;

        if (recipe != null) {
            requiresStar = recipe.requiresVoidStar();
            ratePerRpm = recipe.mbPerTickPerRpm();
        } else if (isVanillaFluid(aboveFluid)) {
            requiresStar = false;
            ratePerRpm = RandomBulkSheetConfig.EXTRACTOR_VANILLA_FLUID_RATE_PER_RPM.get().floatValue();
        } else {
            requiresStar = true;
            ratePerRpm = RandomBulkSheetConfig.EXTRACTOR_NON_VANILLA_FLUID_RATE_PER_RPM.get().floatValue();
        }

        if (RandomBulkSheetConfig.EXTRACTOR_ENFORCE_VOID_STAR.get() && requiresStar && !hasVoidStar)
            return;

        bufferedFluid = aboveFluid;
        fluidAmount = (float) Math.min(maxBuffer, fluidAmount + rpm * ratePerRpm);
    }

    private static boolean isVanillaFluid(Fluid fluid) {
        var holder = BuiltInRegistries.FLUID.wrapAsHolder(fluid);
        return holder.is(FluidTags.WATER) || holder.is(FluidTags.LAVA);
    }

    private FluidExtractingRecipe findRecipeFor(Fluid fluid) {
        if (level == null)
            return null;
        return level.getRecipeManager()
                .getAllRecipesFor(RandomBulkSheetRecipes.FLUID_EXTRACTING_TYPE.get())
                .stream()
                .map(holder -> holder.value())
                .filter(r -> r.matchesFluid(fluid))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (fluidAmount <= 0 || bufferedFluid == Fluids.EMPTY)
            return;
        if (level == null || level.isClientSide)
            return;

        IFluidHandler target = level.getCapability(
                Capabilities.FluidHandler.BLOCK, worldPosition.below(), Direction.UP);

        if (target != null)
            pushIntoNeighbor(target);
        else
            distributePressureDown();
        sendData();
    }

    private void pushIntoNeighbor(IFluidHandler target) {
        FluidStack toSend = new FluidStack(bufferedFluid, (int) fluidAmount);

        int simulated = target.fill(toSend.copy(), IFluidHandler.FluidAction.SIMULATE);
        if (simulated <= 0)
            return;

        int filled = target.fill(new FluidStack(bufferedFluid, simulated), IFluidHandler.FluidAction.EXECUTE);
        if (filled <= 0)
            return;

        fluidAmount -= filled;
        if (fluidAmount <= 0) {
            fluidAmount = 0;
            bufferedFluid = Fluids.EMPTY;
        }

        setChanged();
    }

    protected void distributePressureDown() {
        Direction side = PUSH_SIDE;
        BlockFace start = new BlockFace(worldPosition, side);
        boolean pull = false;

        Set<BlockFace> targets = new HashSet<>();
        Map<BlockPos, Pair<Integer, Map<Direction, Boolean>>> pipeGraph = new HashMap<>();

        FluidPropagator.resetAffectedFluidNetworks(level, worldPosition, side.getOpposite());

        if (!hasReachedValidEndpoint(level, start, pull)) {
            pipeGraph.computeIfAbsent(worldPosition, $ -> Pair.of(0, new IdentityHashMap<>()))
                    .getSecond()
                    .put(side, pull);
            pipeGraph.computeIfAbsent(start.getConnectedPos(), $ -> Pair.of(1, new IdentityHashMap<>()))
                    .getSecond()
                    .put(side.getOpposite(), !pull);

            List<Pair<Integer, BlockPos>> frontier = new ArrayList<>();
            Set<BlockPos> visited = new HashSet<>();
            int maxDistance = FluidPropagator.getPumpRange();
            frontier.add(Pair.of(1, start.getConnectedPos()));

            while (!frontier.isEmpty()) {
                Pair<Integer, BlockPos> entry = frontier.remove(0);
                int distance = entry.getFirst();
                BlockPos currentPos = entry.getSecond();

                if (!level.isLoaded(currentPos))
                    continue;
                if (visited.contains(currentPos))
                    continue;
                visited.add(currentPos);

                BlockState currentState = level.getBlockState(currentPos);
                FluidTransportBehaviour pipe = FluidPropagator.getPipe(level, currentPos);
                if (pipe == null)
                    continue;

                for (Direction face : FluidPropagator.getPipeConnections(currentState, pipe)) {
                    BlockFace blockFace = new BlockFace(currentPos, face);
                    BlockPos connectedPos = blockFace.getConnectedPos();

                    if (!level.isLoaded(connectedPos))
                        continue;
                    if (blockFace.isEquivalent(start))
                        continue;

                    if (hasReachedValidEndpoint(level, blockFace, pull)) {
                        pipeGraph.computeIfAbsent(currentPos, $ -> Pair.of(distance, new IdentityHashMap<>()))
                                .getSecond()
                                .put(face, pull);
                        targets.add(blockFace);
                        continue;
                    }

                    FluidTransportBehaviour pipeBehaviour = FluidPropagator.getPipe(level, connectedPos);
                    if (pipeBehaviour == null)
                        continue;
                    if (visited.contains(connectedPos))
                        continue;

                    if (distance + 1 >= maxDistance) {
                        pipeGraph.computeIfAbsent(currentPos, $ -> Pair.of(distance, new IdentityHashMap<>()))
                                .getSecond()
                                .put(face, pull);
                        targets.add(blockFace);
                        continue;
                    }

                    pipeGraph.computeIfAbsent(currentPos, $ -> Pair.of(distance, new IdentityHashMap<>()))
                            .getSecond()
                            .put(face, pull);
                    pipeGraph.computeIfAbsent(connectedPos, $ -> Pair.of(distance + 1, new IdentityHashMap<>()))
                            .getSecond()
                            .put(face.getOpposite(), !pull);
                    frontier.add(Pair.of(distance + 1, connectedPos));
                }
            }
        }

        Map<Integer, Set<BlockFace>> validFaces = new HashMap<>();
        searchForEndpointRecursively(pipeGraph, targets, validFaces,
                new BlockFace(start.getPos(), start.getOppositeFace()), pull);

        for (Set<BlockFace> set : validFaces.values()) {
            int parallelBranches = Math.max(1, set.size() - 1);
            for (BlockFace face : set) {
                BlockPos pipePos = face.getPos();
                Direction pipeSide = face.getFace();
                if (pipePos.equals(worldPosition))
                    continue;

                boolean inbound = pipeGraph.get(pipePos).getSecond().get(pipeSide);
                FluidTransportBehaviour pipeBehaviour = FluidPropagator.getPipe(level, pipePos);
                if (pipeBehaviour == null)
                    continue;

                pipeBehaviour.addPressure(pipeSide, inbound, getMaxBuffer() / parallelBranches);
            }
        }
    }

    protected boolean searchForEndpointRecursively(Map<BlockPos, Pair<Integer, Map<Direction, Boolean>>> pipeGraph,
                                                   Set<BlockFace> targets, Map<Integer, Set<BlockFace>> validFaces,
                                                   BlockFace currentFace, boolean pull) {
        BlockPos currentPos = currentFace.getPos();
        if (!pipeGraph.containsKey(currentPos))
            return false;

        Pair<Integer, Map<Direction, Boolean>> pair = pipeGraph.get(currentPos);
        int distance = pair.getFirst();
        boolean atLeastOneBranchSuccessful = false;

        for (Direction nextFacing : Iterate.directions) {
            if (nextFacing == currentFace.getFace())
                continue;

            Map<Direction, Boolean> map = pair.getSecond();
            if (!map.containsKey(nextFacing))
                continue;

            BlockFace localTarget = new BlockFace(currentPos, nextFacing);
            if (targets.contains(localTarget)) {
                validFaces.computeIfAbsent(distance, $ -> new HashSet<>()).add(localTarget);
                atLeastOneBranchSuccessful = true;
                continue;
            }

            if (map.get(nextFacing) != pull)
                continue;

            if (!searchForEndpointRecursively(pipeGraph, targets, validFaces,
                    new BlockFace(currentPos.relative(nextFacing), nextFacing.getOpposite()), pull))
                continue;

            validFaces.computeIfAbsent(distance, $ -> new HashSet<>()).add(localTarget);
            atLeastOneBranchSuccessful = true;
        }

        if (atLeastOneBranchSuccessful)
            validFaces.computeIfAbsent(distance, $ -> new HashSet<>()).add(currentFace);

        return atLeastOneBranchSuccessful;
    }

    private boolean hasReachedValidEndpoint(LevelAccessor world, BlockFace blockFace, boolean pull) {
        BlockPos connectedPos = blockFace.getConnectedPos();
        BlockState connectedState = world.getBlockState(connectedPos);
        BlockEntity blockEntity = world.getBlockEntity(connectedPos);
        Direction face = blockFace.getFace();

        FluidTransportBehaviour pipe = FluidPropagator.getPipe(world, connectedPos);
        if (pipe != null && pipe.canHaveFlowToward(connectedState, blockFace.getOppositeFace()))
            return false;

        if (blockEntity != null) {
            IFluidHandler capability = blockEntity.getLevel()
                    .getCapability(Capabilities.FluidHandler.BLOCK, blockEntity.getBlockPos(), face.getOpposite());
            if (capability != null)
                return true;
        }

        return FluidPropagator.isOpenEnd(world, blockFace.getPos(), face);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        hasVoidStar = tag.getBoolean("HasVoidStar");
        fluidAmount = tag.getFloat("FluidAmount");

        String fluidId = tag.getString("BufferedFluid");
        if (!fluidId.isEmpty()) {
            ResourceLocation id = ResourceLocation.tryParse(fluidId);
            bufferedFluid = (id != null && BuiltInRegistries.FLUID.containsKey(id))
                    ? BuiltInRegistries.FLUID.get(id)
                    : Fluids.EMPTY;
        } else {
            bufferedFluid = Fluids.EMPTY;
        }

        super.read(tag, registries, clientPacket);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        tag.putBoolean("HasVoidStar", hasVoidStar);
        tag.putFloat("FluidAmount", fluidAmount);
        if (bufferedFluid != Fluids.EMPTY)
            tag.putString("BufferedFluid", BuiltInRegistries.FLUID.getKey(bufferedFluid).toString());
        super.write(tag, registries, clientPacket);
    }
    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        tooltip.add(Component.empty());
        CreateLang.builder()
                .text("Fluid Extracting Info:")
                .style(ChatFormatting.WHITE)
                .forGoggles(tooltip);

        // ---- Buffered ----
        CreateLang.builder()
                .text("Buffered:")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        if (bufferedFluid != Fluids.EMPTY && fluidAmount > 0) {
            String fluidName = bufferedFluid.getFluidType().getDescription().getString();
            CreateLang.builder()
                    .add(CreateLang.builder().text(fluidName).style(ChatFormatting.GOLD))
                    .add(CreateLang.builder().text(" " + String.format("%.1fmB", fluidAmount)).style(ChatFormatting.BLUE))
                    .forGoggles(tooltip, 1);
        } else {
            CreateLang.builder()
                    .text("Empty")
                    .style(ChatFormatting.GOLD)
                    .forGoggles(tooltip, 1);
        }

        // ---- Extracting ----
        if (level != null) {
            FluidState aboveState = level.getFluidState(worldPosition.above());

            if (!aboveState.isEmpty()) {
                Fluid aboveFluid = aboveState.getType();
                String aboveFluidName = aboveFluid.getFluidType().getDescription().getString();

                FluidExtractingRecipe recipe = findRecipeFor(aboveFluid);

                boolean requiresStar;
                float ratePerRpm;

                if (recipe != null) {
                    requiresStar = recipe.requiresVoidStar();
                    ratePerRpm = recipe.mbPerTickPerRpm();
                } else if (isVanillaFluid(aboveFluid)) {
                    requiresStar = false;
                    ratePerRpm = RandomBulkSheetConfig.EXTRACTOR_VANILLA_FLUID_RATE_PER_RPM.get().floatValue();
                } else {
                    requiresStar = true;
                    ratePerRpm = RandomBulkSheetConfig.EXTRACTOR_NON_VANILLA_FLUID_RATE_PER_RPM.get().floatValue();
                }

                boolean starEnforced = RandomBulkSheetConfig.EXTRACTOR_ENFORCE_VOID_STAR.get();
                boolean blocked = starEnforced && requiresStar && !hasVoidStar;

                CreateLang.builder()
                        .text("Extracting:")
                        .style(ChatFormatting.GRAY)
                        .forGoggles(tooltip);

                if (blocked) {
                    CreateLang.builder()
                            .add(CreateLang.builder().text(aboveFluidName).style(ChatFormatting.GOLD))
                            .add(CreateLang.builder().text(" Cannot extract!").style(ChatFormatting.RED))
                            .forGoggles(tooltip, 1);

                    CreateLang.builder()
                            .add(CreateLang.builder().text("This Fluid requires a ").style(ChatFormatting.RED))
                            .add(CreateLang.builder().text("Void Star").style(ChatFormatting.LIGHT_PURPLE))
                            .add(CreateLang.builder().text(" for extraction.").style(ChatFormatting.RED))
                            .forGoggles(tooltip, 1);
                } else {
                    float rpm = Math.abs(getSpeed());
                    float currentRate = rpm * ratePerRpm;

                    CreateLang.builder()
                            .add(CreateLang.builder().text(aboveFluidName).style(ChatFormatting.GOLD))
                            .add(CreateLang.builder().text(" " + String.format("%.4fmB", currentRate)).style(ChatFormatting.AQUA))
                            .add(CreateLang.builder().text(" per tick").style(ChatFormatting.GRAY))
                            .forGoggles(tooltip, 1);
                }
            }
        }

        return true;
    }

    class ExtractorPumpBehaviour extends FluidTransportBehaviour {

        public ExtractorPumpBehaviour(SmartBlockEntity be) {
            super(be);
        }

        @Override
        public void tick() {
            super.tick();
            var connection = interfaces.get(PUSH_SIDE);
            if (connection == null)
                return;

            boolean hasFluidToSend = bufferedFluid != Fluids.EMPTY && fluidAmount > 0;
            Couple<Float> pressure = connection.getPressure();
            pressure.set(false, hasFluidToSend ? getMaxBuffer() : 0f);
            pressure.set(true, 0f);
        }

        @Override
        public boolean canHaveFlowToward(BlockState state, Direction direction) {
            return direction == PUSH_SIDE;
        }

        @Override
        public FluidStack getProvidedOutwardFluid(Direction side) {
            if (side != PUSH_SIDE || bufferedFluid == Fluids.EMPTY || fluidAmount <= 0)
                return FluidStack.EMPTY;
            return new FluidStack(bufferedFluid, Math.max(1, (int) fluidAmount));
        }
    }
}

