package net.minecraft.server;

import com.legacyminecraft.poseidon.PoseidonConfig;

import java.util.Random;

public class ItemDye extends Item {

    public static final String[] a = new String[] { "black", "red", "green", "brown", "blue", "purple", "cyan", "silver", "gray", "pink", "lime", "yellow", "lightBlue", "magenta", "orange", "white"};
    public static final int[] bk = new int[] { 1973019, 11743532, 3887386, 5320730, 2437522, 8073150, 2651799, 2651799, 4408131, 14188952, 4312372, 14602026, 6719955, 12801229, 15435844, 15790320};

    public ItemDye(int i) {
        super(i);
        this.a(true);
        this.d(0);
    }

    public boolean a(ItemStack itemstack, EntityHuman entityhuman, World world, int i, int j, int k, int l) {
        if (itemstack.getData() == 15) {
            int i1 = world.getTypeId(i, j, k);

            if (i1 == Block.SAPLING.id) {
                if (!world.isStatic) {
                    ((BlockSapling) Block.SAPLING).b(world, i, j, k, world.random);
                    --itemstack.count;
                }

                return true;
            }

            if (i1 == Block.CROPS.id) {
                if (!world.isStatic) {
                    ((BlockCrops) Block.CROPS).d_(world, i, j, k);
                    --itemstack.count;
                }

                return true;
            }

            if (i1 == Block.GRASS.id) {
                if (!world.isStatic) {
                    --itemstack.count;

                    label53:
                    for (int j1 = 0; j1 < 128; ++j1) {
                        int k1 = i;
                        int l1 = j + 1;
                        int i2 = k;

                        for (int j2 = 0; j2 < j1 / 16; ++j2) {
                            k1 += b.nextInt(3) - 1;
                            l1 += (b.nextInt(3) - 1) * b.nextInt(3) / 2;
                            i2 += b.nextInt(3) - 1;
                            if (world.getTypeId(k1, l1 - 1, i2) != Block.GRASS.id || world.e(k1, l1, i2)) {
                                continue label53;
                            }
                        }

                        if (world.getTypeId(k1, l1, i2) == 0) {
                            if (b.nextInt(10) != 0) {
                                if (PoseidonConfig.getInstance().getConfigBoolean("world-settings.bone-meal-ferns.enabled", false)
                                && world.getWorldChunkManager().getBiome(k1, i2).n.equals("Rainforest")) {
                                    int meta = new Random().nextInt(2) == 0 ? 1 : 2;
                                    world.setTypeIdAndData(k1, l1, i2, Block.LONG_GRASS.id, meta);
                                } else {
                                    world.setTypeIdAndData(k1, l1, i2, Block.LONG_GRASS.id, 1);
                                }
                            } else if (b.nextInt(3) != 0) {
                                world.setTypeId(k1, l1, i2, Block.YELLOW_FLOWER.id);
                            } else {
                                world.setTypeId(k1, l1, i2, Block.RED_ROSE.id);
                            }
                        }
                    }
                }

                return true;
            }
        }

        return false;
    }

    public void a(ItemStack itemstack, EntityLiving entityliving) {
        if (entityliving instanceof EntitySheep) {
            EntitySheep entitysheep = (EntitySheep) entityliving;
            int i = BlockCloth.c(itemstack.getData());

            if (!entitysheep.isSheared() && entitysheep.getColor() != i) {
                entitysheep.setColor(i);
                --itemstack.count;
            }
        }
    }
}
