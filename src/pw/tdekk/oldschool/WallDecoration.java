package pw.tdekk.oldschool;

import pw.tdekk.visitor.GraphVisitor;
import pw.tdekk.visitor.VisitorInfo;
import pw.tdekk.mod.hooks.FieldHook;
import org.objectweb.asm.commons.cfg.Block;
import org.objectweb.asm.commons.cfg.BlockVisitor;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.VariableNode;
import org.objectweb.asm.tree.ClassNode;

@VisitorInfo(hooks = {"x", "y", "plane", "orientation", "id", "flags", "model", "backup"})
public class WallDecoration extends GraphVisitor {

    @Override
    public boolean validate(ClassNode cn) {
        return cn.ownerless() && cn.getFieldTypeCount() == 2 && cn.fieldCount("I") == 9 &&
                cn.fieldCount(desc("RenderableNode")) == 2;
    }

    @Override
    public void visit() {
        visit("Region", new WallHooks());
    }

    private class WallHooks extends BlockVisitor {

        private int added = 0;

        @Override
        public boolean validate() {
            return added < 8;
        }

        @Override
        public void visit(Block block) {
            block.tree().accept(new NodeVisitor(this) {
                public void visitField(FieldMemberNode fmn) {
                    if (fmn.opcode() == PUTFIELD && fmn.owner().equals(cn.name)) {
                        if (fmn.desc().equals("I")) {
                            VariableNode vn = (VariableNode) fmn.layer(IMUL, ILOAD);
                            if (vn == null) {
                                vn = (VariableNode) fmn.layer(IADD, IMUL, ILOAD);
                            }
                            if (vn != null) {
                                String name = null;
                                if (vn.var() == 2) {
                                    name = "x";
                                } else if (vn.var() == 3) {
                                    name = "y";
                                } else if (vn.var() == 4) {
                                    name = "plane";
                                } else if (vn.var() == 9) {
                                    name = "orientation";
                                } else if (vn.var() == 11) {
                                    name = "id";
                                } else if (vn.var() == 12) {
                                    name = "flags";
                                }
                                if (name == null) {
                                    return;
                                }
                                hooks.put(name, new FieldHook(name, fmn.fin()));
                                added++;
                            }
                        } else if (fmn.desc().equals(desc("RenderableNode"))) {
                            VariableNode vn = fmn.firstVariable();
                            if (vn != null) {
                                vn = vn.nextVariable();
                            }
                            if (vn != null) {
                                String name = null;
                                if (vn.var() == 5) {
                                    name = "model";
                                } else if (vn.var() == 6) {
                                    name = "backup";
                                }
                                if (name == null) {
                                    return;
                                }
                                hooks.put(name, new FieldHook(name, fmn.fin()));
                                added++;
                            }
                        }
                    }
                }
            });
        }
    }
}
