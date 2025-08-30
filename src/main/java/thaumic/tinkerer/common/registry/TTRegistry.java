package thaumic.tinkerer.common.registry;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;

import com.google.common.reflect.ClassPath;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import thaumic.tinkerer.client.lib.LibResources;
import thaumic.tinkerer.common.core.handler.ModCreativeTab;
import thaumic.tinkerer.common.item.kami.ItemBlockTalisman;
import thaumic.tinkerer.common.item.kami.ItemPlacementMirror;
import thaumic.tinkerer.common.research.IRegisterableResearch;

public class TTRegistry {

    private final HashMap<Class, Item[]> itemRegistry = new HashMap<>();

    private final HashMap<Class, Block[]> blockRegistry = new HashMap<>();

    public static Item itemPlacementMirror;
    public static Item itemBlackHoleTalisman;

    @SuppressWarnings("UnstableApiUsage")
    public void preInit() {
        try {
            ClassPath classPath = ClassPath.from(this.getClass().getClassLoader());
            for (ClassPath.ClassInfo classInfo : classPath
                    .getTopLevelClassesRecursive("thaumic.tinkerer.common.block")) {
                Class<?> clazz = classInfo.load();
                if (ITTinkererBlock.class.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers())) {
                    loadBlock(clazz);
                }
            }
            for (ClassPath.ClassInfo classInfo : classPath
                    .getTopLevelClassesRecursive("thaumic.tinkerer.common.item")) {
                Class<?> clazz = classInfo.load();
                if (ITTinkererItem.class.isAssignableFrom(clazz) && !ItemBlock.class.isAssignableFrom(clazz)
                        && !Modifier.isAbstract(clazz.getModifiers())) {
                    loadItem(clazz);
                }
            }
            for (Block[] blocks : blockRegistry.values()) {
                for (Block block : blocks) {
                    registerBlock(block, (ITTinkererBlock) block);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        itemBlackHoleTalisman = getFirstItemFromClass(ItemBlockTalisman.class);
        itemPlacementMirror = getFirstItemFromClass(ItemPlacementMirror.class);
    }

    public void registerResearch(ITTinkererRegisterable nextItem) {
        IRegisterableResearch registerableResearch = nextItem.getResearchItem();
        if (registerableResearch != null) {
            registerableResearch.registerResearch();
        }
    }

    public void registerRecipe(ITTinkererRegisterable nextItem) {
        ThaumicTinkererRecipe thaumicTinkererRecipe = nextItem.getRecipeItem();
        if (thaumicTinkererRecipe != null) {
            thaumicTinkererRecipe.registerRecipe();
        }
    }

    private void loadBlock(Class<?> clazz) {
        try {
            final Block newBlock = (Block) clazz.newInstance();
            final ITTinkererBlock ittBlock = (ITTinkererBlock) newBlock;
            if (ittBlock.shouldRegister()) {
                newBlock.setBlockName(ittBlock.getBlockName());

                ArrayList<Object> specialParameters = ittBlock.getSpecialParameters();
                int capacity = 1;
                if (specialParameters != null) {
                    capacity += specialParameters.size();
                }

                Block[] blockList = new Block[capacity];
                blockList[0] = newBlock;

                if (specialParameters != null) {
                    Class<?>[] parameterTypes;
                    int index = 1;
                    for (Object param : specialParameters) {

                        for (Constructor<?> constructor : clazz.getConstructors()) {
                            parameterTypes = constructor.getParameterTypes();
                            if (parameterTypes.length > 0 && parameterTypes[0].isAssignableFrom(param.getClass())) {
                                Block nextBlock = (Block) clazz.getConstructor(param.getClass()).newInstance(param);
                                nextBlock.setBlockName(((ITTinkererBlock) nextBlock).getBlockName());
                                blockList[index++] = nextBlock;
                                break;
                            }
                        }
                    }
                }
                blockRegistry.put(clazz, blockList);

                Class<? extends ItemBlock> itemBlock = ittBlock.getItemBlock();
                if (itemBlock != null) {
                    Item newItem = itemBlock.getConstructor(Block.class).newInstance(newBlock);
                    newItem.setUnlocalizedName(((ITTinkererItem) newItem).getItemName());
                    itemRegistry.put(itemBlock, new Item[] { newItem });
                }
            }
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException
                | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void loadItem(Class<?> clazz) {
        try {
            final Item newItem = (Item) clazz.newInstance();
            final ITTinkererItem ittItem = (ITTinkererItem) newItem;
            if (ittItem.shouldRegister()) {
                newItem.setUnlocalizedName(ittItem.getItemName());

                ArrayList<Object> specialParameters = ittItem.getSpecialParameters();

                int capacity = 1;
                if (specialParameters != null) {
                    capacity += specialParameters.size();
                }

                Item[] itemList = new Item[capacity];
                itemList[0] = newItem;

                if (specialParameters != null) {
                    int index = 1;
                    Class<?>[] parameterTypes;
                    for (Object param : specialParameters) {
                        for (Constructor<?> constructor : clazz.getConstructors()) {
                            parameterTypes = constructor.getParameterTypes();
                            if (parameterTypes.length > 0 && parameterTypes[0].isAssignableFrom(param.getClass())) {
                                Item nextItem = (Item) constructor.newInstance(param);
                                nextItem.setUnlocalizedName(ittItem.getItemName());
                                itemList[index++] = nextItem;
                                break;
                            }
                        }
                    }
                }
                itemRegistry.put(clazz, itemList);
            }
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void registerBlock(Block block, ITTinkererBlock ittBlock) {
        String blockName = ittBlock.getBlockName();
        Class<? extends ItemBlock> itemBlock = ittBlock.getItemBlock();
        if (itemBlock != null) {
            GameRegistry.registerBlock(block, itemBlock, blockName);
        } else {
            GameRegistry.registerBlock(block, blockName);
        }
        Class<? extends TileEntity> tileEntity = ittBlock.getTileEntity();
        if (tileEntity != null) {
            GameRegistry.registerTileEntity(tileEntity, LibResources.PREFIX_MOD + blockName);
        }
        if (block instanceof IMultiTileEntityBlock) {
            for (Map.Entry<Class<? extends TileEntity>, String> tile : ((IMultiTileEntityBlock) block)
                    .getAdditionalTileEntities().entrySet()) {
                GameRegistry.registerTileEntity(tile.getKey(), tile.getValue());
            }
        }
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            if (ittBlock.shouldDisplayInTab()) {
                ModCreativeTab.INSTANCE.addBlock(block);
            }
        }
    }

    public Item[] getItemArrayFromClass(Class clazz) {
        return itemRegistry.get(clazz);
    }

    public Item getItemFromClass(Class clazz, int index) {
        return itemRegistry.get(clazz)[index];
    }

    public Item getFirstItemFromClass(Class clazz) {
        final Item[] items = itemRegistry.get(clazz);
        return items != null ? items[0] : null;
    }

    public Item getItemFromClassAndName(Class clazz, String s) {
        final Item[] itemsFromClass = itemRegistry.get(clazz);
        if (itemsFromClass != null) {
            for (Item i : itemsFromClass) {
                if (((ITTinkererItem) i).getItemName().equals(s)) {
                    return i;
                }
            }
        }
        return null;
    }

    public Block getBlockFromClassAndName(Class clazz, String s) {
        final Block[] blocksFromClass = blockRegistry.get(clazz);
        if (blocksFromClass != null) {
            for (Block i : blocksFromClass) {
                if (((ITTinkererBlock) i).getBlockName().equals(s)) {
                    return i;
                }
            }
        }
        return null;
    }

    public Block[] getBlockArrayFromClass(Class clazz) {
        return blockRegistry.get(clazz);
    }

    public Block getBlockFromClass(Class clazz, int index) {
        return blockRegistry.get(clazz)[index];
    }

    public Block getFirstBlockFromClass(Class clazz) {
        final Block[] blocks = blockRegistry.get(clazz);
        return blocks != null ? blocks[0] : null;
    }

    public void init() {
        for (Item[] itemArrayList : itemRegistry.values()) {
            for (Item item : itemArrayList) {
                registerRecipe((ITTinkererRegisterable) item);

                if (!(item instanceof ItemBlock)) {
                    GameRegistry.registerItem(item, ((ITTinkererItem) item).getItemName());

                    if (((ITTinkererItem) item).shouldDisplayInTab()
                            && FMLCommonHandler.instance().getSide() == Side.CLIENT) {
                        ModCreativeTab.INSTANCE.addItem(item);
                    }
                }
            }
        }

        for (Block[] blockArrayList : blockRegistry.values()) {
            for (Block block : blockArrayList) {
                registerRecipe((ITTinkererRegisterable) block);
            }
        }
        ModCreativeTab.INSTANCE.addAllItemsAndBlocks();
    }

    public void postInit() {
        for (Item[] itemArrayList : itemRegistry.values()) {
            for (Item item : itemArrayList) {
                registerResearch((ITTinkererRegisterable) item);
            }
        }
        for (Block[] blockArrayList : blockRegistry.values()) {
            for (Block block : blockArrayList) {
                registerResearch((ITTinkererRegisterable) block);
            }
        }
    }
}
