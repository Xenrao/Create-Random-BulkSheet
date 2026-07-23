package net.xenrao.create_random_bulksheet.radio;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.foundation.block.IBE;
import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RadioBlock extends HorizontalDirectionalBlock implements IBE<RadioBlockEntity> {

    public RadioBlock(Properties properties) {
        super(properties);
    }

    public static final MapCodec<RadioBlock> CODEC = simpleCodec(RadioBlock::new);

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public Class<RadioBlockEntity> getBlockEntityClass() {
        return RadioBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends RadioBlockEntity> getBlockEntityType() {
        return RadioRegister.RADIO_BE.get();
    }

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0, 14, 16, 12, 16)
    );

    private static final VoxelShaper SHAPER = VoxelShaper.forDirectional(SHAPE, Direction.NORTH);

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPER.get(state.getValue(FACING));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder.add(FACING));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        Direction facing = pContext.getHorizontalDirection().getOpposite();
        boolean reverse = pContext.getPlayer() != null && pContext.getPlayer().isShiftKeyDown();
        return super.getStateForPlacement(pContext).setValue(FACING, reverse ? facing.getOpposite() : facing);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (level.getBlockEntity(pos) instanceof RadioBlockEntity be) {
            if (hitResult.getDirection() == state.getValue(FACING)) {

                Vec3 hit = hitResult.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
                Direction facing = state.getValue(FACING);

                // 0.0 - 1.0 arası değeri 0.0 - 16.0 (pixel) arasına çeviriyoruz
                double u = 0;
                double v = hit.y * 16.0;

                if (facing == Direction.NORTH) u = hit.x * 16.0;
                else if (facing == Direction.SOUTH) u = (1.0 - hit.x) * 16.0;
                else if (facing == Direction.EAST) u = (1.0 - hit.z) * 16.0;
                else if (facing == Direction.WEST) u = hit.z * 16.0;

                // Buton alanları kontrolü
                if (v >= 3.0 && v <= 5.0) {
                    if (u >= 4.0 && u <= 6.0) {
                        be.cycleTrack(player, -1);
                        return InteractionResult.CONSUME;
                    }
                    else if (u >= 7.0 && u <= 9.0) {
                        be.togglePlay(player);
                        return InteractionResult.CONSUME;
                    }
                    else if (u >= 10.0 && u <= 12.0) {
                        be.cycleTrack(player, 1);
                        return InteractionResult.CONSUME;
                    }
                }
            }

            // Eğer butonlara değil de bloğun boş bir yerine tıklandıysa (Senin yorum attığın yer)
            if (player.isShiftKeyDown()) {
                be.cycleTrack(player, 1);
            } else {
                be.togglePlay(player);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }
}