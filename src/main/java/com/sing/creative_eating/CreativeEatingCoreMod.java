package com.sing.creative_eating;

import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nullable;
import java.util.Map;

public class CreativeEatingCoreMod implements IFMLLoadingPlugin {
    public static class ASMTransformer implements IClassTransformer {
        private static final String ITEM_FOOD="net.minecraft.item.ItemFood";
        private static final boolean deobf=false;
        private static final String ON_ITEM_RIGHT_CLICK=deobf?"onItemRightClick":"func_77659_a";
        private static final String IS_CREATIVE=deobf?"isCreative":"func_184812_l_";
        private static final String ON_ITEM_USE_FINISH=deobf?"onItemUseFinish":"func_77654_b";
        @Override
        public byte[] transform(String name, String transformedName, byte[] basicClass) {
            if(!transformedName.equals(ITEM_FOOD))return basicClass;
            ClassNode node=new ClassNode();
            ClassReader reader=new ClassReader(basicClass);
            reader.accept(node,0);
            //MAIN LOGIC

            for(MethodNode m : node.methods){
                //Don't support MCP name.
                final String functionName = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(name, m.name, m.desc);
                if (functionName.equals(ON_ITEM_RIGHT_CLICK)) {
                    InsnList insnList = new InsnList();
                    LabelNode startNode = null;
                    LabelNode returnNode = null;
                    int i = 0;
                    for (AbstractInsnNode insn : m.instructions.toArray()) {
                        if (insn instanceof LabelNode) {
                            if (i == 1) {
                                startNode = (LabelNode) insn;
                            }
                            if (i == 2) {
                                returnNode = (LabelNode) insn;
                                break;
                            }
                            ++i;
                        }
                    }
                    //if(var2.isCreative()) var2:playerIn
                    insnList.add(new VarInsnNode(Opcodes.ALOAD, 2));
                    insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/EntityPlayer", IS_CREATIVE, "()Z", false));
                    insnList.add(new JumpInsnNode(Opcodes.IFNE, returnNode));

                    //return new ActionResult(EnumActionResult.SUCCESS, itemstack)
                    m.instructions.insertBefore(startNode, insnList);
                    System.out.println("Creative Eating Reforged Mod injected successfully :)");
                }else if(functionName.equals(ON_ITEM_USE_FINISH)){
                    InsnList insnList = new InsnList();
                    LabelNode startNode = null;
                    LabelNode returnNode=null;
                    final AbstractInsnNode[] insnNodes= m.instructions.toArray();
                    int count=0;
                    for (int i = insnNodes.length - 1; i >= 0; i--) {
                        final AbstractInsnNode insn = insnNodes[i];
                        if (insn instanceof LabelNode) {
                            if (count == 2) {
                                startNode = (LabelNode) insn;
                            }else if(count==1){
                                returnNode=(LabelNode) insn;
                            }
                            count++;
                        }
                    }
                    //if(var4.isCreative()) var4:playerIn
                    insnList.add(new VarInsnNode(Opcodes.ALOAD,3));
                    insnList.add(new TypeInsnNode(Opcodes.INSTANCEOF,"net/minecraft/entity/player/EntityPlayer"));
                    insnList.add(new JumpInsnNode(Opcodes.IFEQ,startNode));
                    insnList.add(new VarInsnNode(Opcodes.ALOAD,3));
                    insnList.add(new TypeInsnNode(Opcodes.CHECKCAST,"net/minecraft/entity/player/EntityPlayer"));
                    insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/EntityPlayer", IS_CREATIVE, "()Z", false));
                    insnList.add(new JumpInsnNode(Opcodes.IFNE,returnNode));
                    m.instructions.insert(startNode,insnList);
                }
            }
            ClassWriter classWriter  = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            node.accept(classWriter);
            return classWriter.toByteArray();
        }
    }
    @Override
    public String[] getASMTransformerClass() {
        return new String[]{ASMTransformer.class.getName()};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}