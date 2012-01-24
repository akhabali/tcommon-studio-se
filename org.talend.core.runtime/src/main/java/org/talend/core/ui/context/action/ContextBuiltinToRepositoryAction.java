// ============================================================================
//
// Copyright (C) 2006-2012 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.core.ui.context.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.runtime.i18n.Messages;
import org.talend.core.ui.context.IContextModelManager;
import org.talend.core.ui.context.model.template.ContextParameterParent;
import org.talend.core.ui.context.model.template.ContextParameterSortedParent;
import org.talend.core.ui.context.model.template.ContextParameterSortedSon;
import org.talend.repository.ui.actions.AContextualAction;

/**
 * DOC xye class global comment. Detailled comment <br/>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 * 
 */
public class ContextBuiltinToRepositoryAction extends AContextualAction {

    public final static String ID = "org.talend.core.ui.context.actions.ContextBuiltinToRepositoryAction"; //$NON-NLS-1$

    private TreeViewer viewer = null;

    private final IContextModelManager modelManager; // modified by hyWang

    private List<IContextParameter> params = new ArrayList<IContextParameter>();

    private IContextManager contextManager; // added by hyWang

    private static final String LABEL = Messages.getString("ConextTemplateComposite.addToRepositoryContextAction.label"); //$NON-NLS-1$

    public ContextBuiltinToRepositoryAction(IContextModelManager modelManager) {
        super();
        this.setText(LABEL);
        this.setToolTipText(LABEL);
        this.modelManager = modelManager;
    }

    @Override
    protected void doRun() {
        if (contextManager != null) {
            CoreRuntimePlugin.getInstance().getRepositoryService()
                    .openRepositoryReviewDialog(ERepositoryObjectType.CONTEXT, null, params, contextManager);
            viewer.refresh();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.commons.ui.swt.actions.ITreeContextualAction#init(org.eclipse.jface.viewers.TreeViewer,
     * org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        this.viewer = viewer;
        this.contextManager = modelManager.getContextManager();
        boolean canWork = viewer != null && selection != null && selection.size() > 0;
        if (canWork) {
            for (Iterator<?> iter = selection.iterator(); iter.hasNext();) {
                Object object = iter.next();
                if (object instanceof ContextParameterSortedParent) {
                    ContextParameterSortedParent param = (ContextParameterSortedParent) object;
                    if (!IContextParameter.BUILT_IN.equals(param.getSourceId())) {
                        setEnabled(false);
                        return;
                    } else {
                        params.add(param.getParameter());
                    }
                } else if (object instanceof ContextParameterSortedSon) {
                    ContextParameterSortedSon param = (ContextParameterSortedSon) object;
                    if (!IContextParameter.BUILT_IN.equals(param.getParameter().getSource())) {
                        setEnabled(false);
                        return;
                    } else {
                        params.add(param.getParameter());
                    }
                } else if (object instanceof ContextParameterParent) {
                    ContextParameterParent param = (ContextParameterParent) object;
                    if (!IContextParameter.BUILT_IN.equals(param.getParameter().getSource())) {
                        setEnabled(false);
                        return;
                    } else {
                        params.add(param.getParameter());
                    }
                }
            }
        }
        setEnabled(canWork);
    }

}
