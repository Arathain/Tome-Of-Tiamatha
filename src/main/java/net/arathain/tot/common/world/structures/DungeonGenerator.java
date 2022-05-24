package net.arathain.tot.common.world.structures;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import net.arathain.tot.TomeOfTiamatha;
import net.minecraft.block.JigsawBlock;
import net.minecraft.structure.*;
import net.minecraft.structure.piece.PoolStructurePiece;
import net.minecraft.structure.pool.*;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Holder;
import net.minecraft.util.Identifier;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.LegacySimpleRandom;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.*;
import java.util.function.Predicate;

public class DungeonGenerator {


    public static Optional<StructurePiecesGenerator<StructurePoolFeatureConfig>> generate(StructurePiecesGeneratorFactory.Context<StructurePoolFeatureConfig> inContext, PieceFactory pieceFactory, BlockPos pos) {
        int size = 50;

        DynamicRegistryManager registryManager = inContext.registryManager();
        Registry<StructurePool> registry = registryManager.get(Registry.STRUCTURE_POOL_KEY);
        StructurePool structurePool = registry.get(DungeonStructure.START_POOL);

        ChunkRandom chunkRandom = new ChunkRandom(new LegacySimpleRandom(0L));
        chunkRandom.setCarverSeed(inContext.seed(), inContext.chunkPos().x, inContext.chunkPos().z);

        StructurePoolElement startingElement = structurePool.getRandomElement(chunkRandom);
        if (startingElement == EmptyPoolElement.INSTANCE)
            return Optional.empty();

        ChunkGenerator chunkGenerator = inContext.chunkGenerator();
        StructureManager structureManager = inContext.structureManager();
        HeightLimitView heightLimitView = inContext.heightLimitView();
        Predicate<Holder<Biome>> biomePredicate = inContext.validBiome();
        StructureFeature.init();

        BlockRotation blockRotation = BlockRotation.random(chunkRandom);
        PoolStructurePiece poolStructurePiece = pieceFactory.create(structureManager, startingElement, pos, startingElement.getGroundLevelDelta(), blockRotation, startingElement.getBoundingBox(structureManager, pos, blockRotation));
        BlockBox pieceBoundingBox = poolStructurePiece.getBoundingBox();

        int centerX = (pieceBoundingBox.getMaxX() + pieceBoundingBox.getMinX()) / 2;
        int centerZ = (pieceBoundingBox.getMaxZ() + pieceBoundingBox.getMinZ()) / 2;
        int y = pos.getY() + chunkGenerator.getHeightOnGround(centerX, centerZ, Heightmap.Type.WORLD_SURFACE_WG, heightLimitView);

        Set<Holder<Biome>> biomes = chunkGenerator.getBiomeSource().getBiomesInArea(BiomeCoords.fromBlock(centerX), BiomeCoords.fromBlock(y), BiomeCoords.fromBlock(centerZ),30,chunkGenerator.getMultiNoiseSampler());

        for(Holder<Biome> biome : biomes) {
            if(!biomePredicate.test(biome)) {
                return Optional.empty();
            }
        }

        return Optional.of((structurePiecesCollector, context) -> {
            ArrayList<PoolStructurePiece> list = Lists.newArrayList(poolStructurePiece);

            Box box = new Box(centerX - 280, y - 280, centerZ - 280, centerX + 280 + 1, y + 280 + 1, centerZ + 280 + 1);
            StructurePoolGenerator structurePoolGenerator = new StructurePoolGenerator(registry, size, pieceFactory, chunkGenerator, structureManager, list, chunkRandom);
            structurePoolGenerator.structurePieces.addLast(new StoneholmShapedPoolStructurePiece(poolStructurePiece, new MutableObject<>(VoxelShapes.combineAndSimplify(VoxelShapes.cuboid(box), VoxelShapes.cuboid(Box.from(pieceBoundingBox)), BooleanBiFunction.ONLY_FIRST)), 0, null));

            // Go through all structure pieces in the project.
            while (!structurePoolGenerator.structurePieces.isEmpty()) {
                StoneholmShapedPoolStructurePiece shapedPoolStructurePiece = structurePoolGenerator.structurePieces.removeFirst();
                structurePoolGenerator.generatePiece(shapedPoolStructurePiece.piece, shapedPoolStructurePiece.pieceShape, shapedPoolStructurePiece.currentSize, shapedPoolStructurePiece.sourceBlockPos, heightLimitView);
            }
            list.forEach(structurePiecesCollector::addPiece);
        });
    }

    public interface PieceFactory {
        PoolStructurePiece create(StructureManager var1, StructurePoolElement var2, BlockPos var3, int var4, BlockRotation var5, BlockBox var6);
    }

    static final class StructurePoolGenerator {
        final Registry<StructurePool> registry;
        final int maxSize;
        final PieceFactory pieceFactory;
        final ChunkGenerator chunkGenerator;
        final StructureManager structureManager;
        final List<? super PoolStructurePiece> children;
        final Random random;
        final Deque<StoneholmShapedPoolStructurePiece> structurePieces = Queues.newArrayDeque();

        final StructurePool end_cap;

        StructurePoolGenerator(Registry<StructurePool> registry, int maxSize, PieceFactory pieceFactory, ChunkGenerator chunkGenerator, StructureManager structureManager, List<? super PoolStructurePiece> children, Random random) {
            this.registry = registry;
            this.maxSize = maxSize;
            this.pieceFactory = pieceFactory;
            this.chunkGenerator = chunkGenerator;
            this.structureManager = structureManager;
            this.children = children;
            this.random = random;

            end_cap = registry.get(new Identifier(TomeOfTiamatha.MODID, "end"));
        }

        void generatePiece(PoolStructurePiece piece, MutableObject<VoxelShape> pieceShape, int currentSize, BlockPos sourceStructureBlockPos, HeightLimitView world) {
            StructurePoolElement structurePoolElement = piece.getPoolElement();
            BlockPos sourcePos = piece.getPos();
            BlockRotation sourceRotation = piece.getRotation();
            MutableObject<VoxelShape> mutableObject = new MutableObject<>();
            BlockBox sourceBoundingBox = piece.getBoundingBox();
            int boundsMinY = sourceBoundingBox.getMinY();

            BlockPos sourceBlock = sourcePos.add(sourceStructureBlockPos == null ? BlockPos.ORIGIN : sourceStructureBlockPos);

            // For every structure block in the piece.
            for (Structure.StructureBlockInfo structureBlock : structurePoolElement.getStructureBlockInfos(this.structureManager, sourcePos, sourceRotation, this.random)) {
                if(sourceBlock.equals(structureBlock.pos))
                    continue;

                MutableObject<VoxelShape> structureShape;
                Direction structureBlockFaceDirection = JigsawBlock.getFacing(structureBlock.state);
                BlockPos structureBlockPosition = structureBlock.pos;
                BlockPos structureBlockAimPosition = structureBlockPosition.offset(structureBlockFaceDirection);

                // Get pool that structure block is targeting.
                Identifier structureBlockTargetPoolId = new Identifier(structureBlock.nbt.getString("pool"));
                Optional<StructurePool> targetPool = this.registry.getOrEmpty(structureBlockTargetPoolId);
                if (targetPool.isEmpty() || targetPool.get().getElementCount() == 0 && !Objects.equals(structureBlockTargetPoolId, StructurePools.EMPTY.getValue())) {
                    continue;
                }

                // Get end cap pool for target pool.
                Identifier terminatorPoolId = targetPool.get().getTerminatorsId();
                Optional<StructurePool> terminatorPool = this.registry.getOrEmpty(terminatorPoolId);
                if (terminatorPool.isEmpty() || terminatorPool.get().getElementCount() == 0 && !Objects.equals(terminatorPoolId, StructurePools.EMPTY.getValue())) {
                    continue;
                }

                // Check if target position is inside current piece's bounding box.
                boolean containsPosition = sourceBoundingBox.contains(structureBlockAimPosition);
                if (containsPosition) {
                    structureShape = mutableObject;
                    if (mutableObject.getValue() == null) {
                        mutableObject.setValue(VoxelShapes.cuboid(Box.from(sourceBoundingBox)));
                    }
                } else {
                    structureShape = pieceShape;
                }

                // Get spawnable elements
                ArrayList<StructurePoolElement> possibleElementsToSpawn = Lists.newArrayList();
                if (currentSize < this.maxSize) {
                    possibleElementsToSpawn.addAll(targetPool.get().getElementIndicesInRandomOrder(this.random)); // Add in pool elements if we haven't reached max size.
                }
                possibleElementsToSpawn.addAll(terminatorPool.get().getElementIndicesInRandomOrder(this.random)); // Add in terminator elements.

                for (StructurePoolElement iteratedStructureElement : possibleElementsToSpawn) {
                    if (iteratedStructureElement == EmptyPoolElement.INSTANCE)
                        break;

                    boolean placed = tryPlacePiece(piece, currentSize, world, boundsMinY, structureBlock, structureShape, structureBlockFaceDirection, structureBlockPosition, structureBlockAimPosition, iteratedStructureElement, currentSize >= 2);
                    if(placed)
                        break;
                }
            }
        }

        // Returns true if we could place piece.
        boolean tryPlacePiece(PoolStructurePiece piece, int currentSize, HeightLimitView world, int boundsMinY, Structure.StructureBlockInfo structureBlock, MutableObject<VoxelShape> structureShape, Direction structureBlockFaceDirection, BlockPos structureBlockPosition, BlockPos structureBlockAimPosition, StructurePoolElement element, boolean doTerrainCheck) {
            int j = structureBlockPosition.getY() - boundsMinY;
            int t = boundsMinY + j;
            int pieceGroundLevelDelta = piece.getGroundLevelDelta();

            for (BlockRotation randomizedRotation : BlockRotation.randomRotationOrder(this.random)) {
                // Get all structure blocks in structure.
                List<Structure.StructureBlockInfo> structureBlocksInStructure = element.getStructureBlockInfos(this.structureManager, BlockPos.ORIGIN, randomizedRotation, this.random);

                // Loop through all blocks in piece we are trying to place.
                for (Structure.StructureBlockInfo structureBlockInfo : structureBlocksInStructure) {
                    // If the attachment ID doesn't match then skip this one.
                    if (!JigsawBlock.attachmentMatches(structureBlock, structureBlockInfo))
                        continue;

                    BlockPos structureBlockPos = structureBlockInfo.pos;
                    BlockPos structureBlockAimDelta = structureBlockAimPosition.subtract(structureBlockPos);
                    BlockBox iteratedStructureBoundingBox = element.getBoundingBox(this.structureManager, structureBlockAimDelta, randomizedRotation);

                    int structureBlockY = structureBlockPos.getY();
                    int o = j - structureBlockY + JigsawBlock.getFacing(structureBlock.state).getOffsetY();
                    int adjustedMinY = boundsMinY + o;
                    int pieceYOffset = adjustedMinY - iteratedStructureBoundingBox.getMinY();
                    BlockBox offsetBoundingBox = iteratedStructureBoundingBox.offset(0, pieceYOffset, 0);

                    // If bounding boxes overlap at all; skip.
                    if (VoxelShapes.matchesAnywhere(structureShape.getValue(), VoxelShapes.cuboid(Box.from(offsetBoundingBox).contract(0.25)), BooleanBiFunction.ONLY_SECOND))
                        continue;

                    //Skip if top of bounding box is above terrain. This is extremely hacky. Like, genuinely this is terrible.
                    if(doTerrainCheck && structureBlockFaceDirection != Direction.DOWN) {
                        int maxYBuffer = 3;
                        int maxY = offsetBoundingBox.getMaxY() + maxYBuffer;

                        boolean minCorner = maxY > chunkGenerator.getHeightOnGround(offsetBoundingBox.getMinX(), offsetBoundingBox.getMinZ(), Heightmap.Type.WORLD_SURFACE_WG, world);
                        boolean maxCorner = maxY > chunkGenerator.getHeightOnGround(offsetBoundingBox.getMaxX(), offsetBoundingBox.getMaxZ(), Heightmap.Type.WORLD_SURFACE_WG, world);
                        boolean minXmaxZ = maxY > chunkGenerator.getHeightOnGround(offsetBoundingBox.getMinX(), offsetBoundingBox.getMaxZ(), Heightmap.Type.WORLD_SURFACE_WG, world);
                        boolean maxXminZ = maxY > chunkGenerator.getHeightOnGround(offsetBoundingBox.getMaxX(), offsetBoundingBox.getMinZ(), Heightmap.Type.WORLD_SURFACE_WG, world);

                        int overTerrainCorners = (minCorner ? 1 : 0) + (minXmaxZ ? 1 : 0) + (maxCorner ? 1 : 0) + (maxXminZ ? 1 : 0);

                        if (overTerrainCorners > 1) {
                            element = end_cap.getRandomElement(random);

                            if (overTerrainCorners > 2) {
                                if(currentSize + 2 > maxSize)
                                    element = end_cap.getRandomElement(random);
                            }

                            // If failing switch pool elements to fallback
                            return tryPlacePiece(piece, currentSize, boundsMinY, structureBlock, structureShape, structureBlockPosition, structureBlockAimPosition, element);
                        }
                    }

                    StructurePool.Projection iteratedProjection = element.getProjection();
                    BlockPos offsetBlockPos = structureBlockAimDelta.add(0, pieceYOffset, 0);

                    // All checks have passed,
                    structureShape.setValue(VoxelShapes.combine(structureShape.getValue(), VoxelShapes.cuboid(Box.from(offsetBoundingBox)), BooleanBiFunction.ONLY_FIRST));

                    int s = pieceGroundLevelDelta - o;
                    PoolStructurePiece poolStructurePiece = this.pieceFactory.create(this.structureManager, element, offsetBlockPos, s, randomizedRotation, offsetBoundingBox);

                    piece.addJunction(new JigsawJunction(structureBlockAimPosition.getX(), t - j + pieceGroundLevelDelta, structureBlockAimPosition.getZ(), o, iteratedProjection));
                    poolStructurePiece.addJunction(new JigsawJunction(structureBlockPosition.getX(), t - structureBlockY + s, structureBlockPosition.getZ(), -o, StructurePool.Projection.RIGID));
                    this.children.add(poolStructurePiece);

                    if (currentSize + 1 <= this.maxSize) // Whilst this is not the end.
                        this.structurePieces.addLast(new StoneholmShapedPoolStructurePiece(poolStructurePiece, structureShape, currentSize + 1, structureBlockPos));

                    return true;
                }
            }

            return false;
        }

        // Returns true if we could place piece.
        boolean tryPlacePiece(PoolStructurePiece piece, int currentSize, int boundsMinY, Structure.StructureBlockInfo structureBlock, MutableObject<VoxelShape> structureShape, BlockPos structureBlockPosition, BlockPos structureBlockAimPosition, StructurePoolElement element) {
            int j = structureBlockPosition.getY() - boundsMinY;
            int t = boundsMinY + j;
            int pieceGroundLevelDelta = piece.getGroundLevelDelta();

            for (BlockRotation randomizedRotation : BlockRotation.randomRotationOrder(this.random)) {
                // Get all structure blocks in structure.
                List<Structure.StructureBlockInfo> structureBlocksInStructure = element.getStructureBlockInfos(this.structureManager, BlockPos.ORIGIN, randomizedRotation, this.random);

                // Loop through all blocks in piece we are trying to place.
                for (Structure.StructureBlockInfo structureBlockInfo : structureBlocksInStructure) {
                    // If the attachment ID doesn't match then skip this one.
                    if (JigsawBlock.attachmentMatches(structureBlock, structureBlockInfo))
                        continue;

                    BlockPos structureBlockPos = structureBlockInfo.pos;
                    BlockPos structureBlockAimDelta = structureBlockAimPosition.subtract(structureBlockPos);
                    BlockBox iteratedStructureBoundingBox = element.getBoundingBox(this.structureManager, structureBlockAimDelta, randomizedRotation);

                    int structureBlockY = structureBlockPos.getY();
                    int o = j - structureBlockY + JigsawBlock.getFacing(structureBlock.state).getOffsetY();
                    int adjustedMinY = boundsMinY + o;
                    int pieceYOffset = adjustedMinY - iteratedStructureBoundingBox.getMinY();
                    BlockBox offsetBoundingBox = iteratedStructureBoundingBox.offset(0, pieceYOffset, 0);

                    // If bounding boxes overlap at all; skip.
                    if (VoxelShapes.matchesAnywhere(structureShape.getValue(), VoxelShapes.cuboid(Box.from(offsetBoundingBox).contract(0.25)), BooleanBiFunction.ONLY_SECOND))
                        continue;

                    StructurePool.Projection iteratedProjection = element.getProjection();
                    BlockPos offsetBlockPos = structureBlockAimDelta.add(0, pieceYOffset, 0);

                    // All checks have passed,
                    structureShape.setValue(VoxelShapes.combine(structureShape.getValue(), VoxelShapes.cuboid(Box.from(offsetBoundingBox)), BooleanBiFunction.ONLY_FIRST));

                    int s = pieceGroundLevelDelta - o;
                    PoolStructurePiece poolStructurePiece = this.pieceFactory.create(this.structureManager, element, offsetBlockPos, s, randomizedRotation, offsetBoundingBox);

                    piece.addJunction(new JigsawJunction(structureBlockAimPosition.getX(), t - j + pieceGroundLevelDelta, structureBlockAimPosition.getZ(), o, iteratedProjection));
                    poolStructurePiece.addJunction(new JigsawJunction(structureBlockPosition.getX(), t - structureBlockY + s, structureBlockPosition.getZ(), -o, StructurePool.Projection.RIGID));
                    this.children.add(poolStructurePiece);

                    if (currentSize + 1 <= this.maxSize) // Whilst this is not the end.
                        this.structurePieces.addLast(new StoneholmShapedPoolStructurePiece(poolStructurePiece, structureShape, currentSize + 1, structureBlockPos));

                    return true;
                }
            }

            return false;
        }
    }

    record StoneholmShapedPoolStructurePiece(PoolStructurePiece piece, MutableObject<VoxelShape> pieceShape, int currentSize, BlockPos sourceBlockPos) {}

}
