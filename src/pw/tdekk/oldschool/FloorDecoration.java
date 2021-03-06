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

@VisitorInfo(hooks = {"x", "y", "plane", "id", "flags", "model"})
public class FloorDecoration extends GraphVisitor {

    @Override
    public boolean validate(ClassNode cn) {
        return cn.ownerless() && cn.getFieldTypeCount() == 2 && cn.fieldCount("I") == 5 &&
                cn.fieldCount(desc("RenderableNode")) == 1;
    }

    @Override
    public void visit() {
        visit("Region", new FloorHooks());
        add("model", cn.getField(null, desc("RenderableNode")));
    }

    private class FloorHooks extends BlockVisitor {

        private int added = 0;

        @Override
        public boolean validate() {
            return added < 5;
        }

        @Override
        public void visit(Block block) {
            block.tree().accept(new NodeVisitor(this) {
                public void visitField(FieldMemberNode fmn) {
                    if (fmn.opcode() == PUTFIELD && fmn.owner().equals(cn.name) && fmn.desc().equals("I")) {
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
                            } else if (vn.var() == 6) {
                                name = "id";
                            } else if (vn.var() == 7) {
                                name = "flags";
                            }
                            if (name == null) {
                                return;
                            }
                            hooks.put(name, new FieldHook(name, fmn.fin()));
                            added++;
                        }
                    }
                }
            });
        }
    }
}