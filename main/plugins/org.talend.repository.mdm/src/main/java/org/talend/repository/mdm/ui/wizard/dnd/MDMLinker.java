// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.mdm.ui.wizard.dnd;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.talend.commons.runtime.xml.XmlNodeRetriever;
import org.talend.commons.runtime.xml.XmlUtil;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.ws.WindowSystem;
import org.talend.commons.ui.swt.dialogs.EventLoopProgressMonitor;
import org.talend.commons.ui.swt.dialogs.ProgressDialog;
import org.talend.commons.ui.swt.drawing.link.BezierHorizontalLink;
import org.talend.commons.ui.swt.drawing.link.ExtremityEastArrow;
import org.talend.commons.ui.swt.drawing.link.ExtremityLink;
import org.talend.commons.ui.swt.drawing.link.IExtremityLink;
import org.talend.commons.ui.swt.drawing.link.IStyleLink;
import org.talend.commons.ui.swt.drawing.link.LinkDescriptor;
import org.talend.commons.ui.swt.drawing.link.StyleLink;
import org.talend.commons.ui.swt.drawing.link.TreeExtremityDescriptor;
import org.talend.commons.ui.swt.extended.table.AbstractExtendedControlViewer.EVENT_TYPE;
import org.talend.commons.ui.swt.extended.table.ExtendedControlEvent;
import org.talend.commons.ui.swt.extended.table.IExtendedControlListener;
import org.talend.commons.ui.swt.linking.TreeToTablesLinker;
import org.talend.commons.ui.swt.proposal.TextCellEditorWithProposal;
import org.talend.commons.ui.swt.tableviewer.IModifiedBeanListener;
import org.talend.commons.ui.swt.tableviewer.ModifiedBeanEvent;
import org.talend.commons.ui.swt.tableviewer.selection.ILineSelectionListener;
import org.talend.commons.ui.swt.tableviewer.selection.LineSelectionEvent;
import org.talend.commons.ui.swt.tableviewer.selection.SelectionHelper;
import org.talend.commons.ui.utils.TableUtils;
import org.talend.commons.utils.data.list.IListenableListListener;
import org.talend.commons.utils.data.list.ListenableListEvent;
import org.talend.commons.utils.data.list.ListenableListEvent.TYPE;
import org.talend.core.model.metadata.builder.connection.Concept;
import org.talend.core.model.metadata.builder.connection.ConceptTarget;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.repository.mdm.i18n.Messages;
import org.talend.repository.mdm.model.MDMXSDExtractorFieldModel;
import org.talend.repository.mdm.model.MDMXSDExtractorLoopModel;
import org.talend.repository.mdm.ui.wizard.table.ExtractionFieldsWithMDMEditorView;
import org.talend.repository.mdm.ui.wizard.table.ExtractionLoopWithMDMEditorView;
import org.talend.repository.mdm.util.MDMUtil;
import org.talend.repository.ui.utils.ConnectionContextHelper;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.TreePopulator;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.extraction.XmlExtractorBgRefresher;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.extraction.XsdNodeRetriever;
import org.w3c.dom.Node;

/**
 * DOC hwang class global comment. Detailled comment
 */
public class MDMLinker extends TreeToTablesLinker<Object, Object> {

    private TreePopulator treePopulator;

    private ExtractionFieldsWithMDMEditorView fieldsTableEditorView;

    private XmlNodeRetriever nodeRetriever;

    private ExtractionLoopWithMDMEditorView loopTableEditorView;

    private StyleLink selectedLoopStyleLink;

    private ArrayList<String> loopXpathNodes = new ArrayList<String>(0);

    private Comparator<LinkDescriptor<TreeItem, Object, Table, Object>> drawingLinksComparator;

    private ArrayList<Node> uniqueLoopNodes = new ArrayList<Node>();

    private ArrayList<Node> allLoopNodes = new ArrayList<Node>();

    private Color selectedLoopLinkColor;

    private Color selectedRelativeLinkColor;

    private boolean isXSDFile;

    private boolean isTemplateExist;

    private String xsdFilepath;

    /**
     * DOC amaumont XmlToMetadataTableLinker constructor comment.
     * 
     * @param commonParent common main parent of tree and table, it and its children should have backgoundMode
     * configured with SWT.INHERIT_FORCE, same configuration for parents of tree and table.
     * @param tree
     * @param loopTableEditorView
     * @param table
     */
    public MDMLinker(Composite commonParent, boolean isTemplateExist) {
        super(commonParent);
        this.isTemplateExist = isTemplateExist;
        File tempTemplateXSDFile = MDMUtil.getTempTemplateXSDFile();
        xsdFilepath = tempTemplateXSDFile.getAbsolutePath();

    }

    public void init(Tree tree, ExtractionLoopWithMDMEditorView loopTableEditorView,
            ExtractionFieldsWithMDMEditorView fieldsTableEditorView, TreePopulator treePopulator) {
        if (!isTemplateExist) {
            return;
        }
        init(tree, new Table[] { loopTableEditorView.getExtendedTableViewer().getTableViewerCreator().getTable(),
                fieldsTableEditorView.getExtendedTableViewer().getTableViewerCreator().getTable(), },
                new XmlExtractorBgRefresher(this));
        this.treePopulator = treePopulator;
        this.loopTableEditorView = loopTableEditorView;
        this.fieldsTableEditorView = fieldsTableEditorView;
        isXSDFile = XmlUtil.isXSDFile(xsdFilepath);
        if (isXSDFile) {
            this.nodeRetriever = new XsdNodeRetriever(xsdFilepath, getCurrentLoopXPath());
            ((XsdNodeRetriever) nodeRetriever).setTreePopulator(treePopulator);
        } else {
            this.nodeRetriever = new XmlNodeRetriever(xsdFilepath, getCurrentLoopXPath());
        }
        TextCellEditorWithProposal xPathCellEditor = loopTableEditorView.getXPathCellEditor();
        xPathCellEditor.setContentProposalProvider(new MDMProposalProvider(this, false));
        xPathCellEditor = fieldsTableEditorView.getXPathCellEditor();
        xPathCellEditor.setContentProposalProvider(new MDMProposalProvider(this, true));
        init();
    }

    public void init(TreePopulator treePopulator) {
        this.treePopulator = treePopulator;
        isXSDFile = XmlUtil.isXSDFile(xsdFilepath);
        if (isXSDFile) {
            this.nodeRetriever = new XsdNodeRetriever(xsdFilepath, getCurrentLoopXPath());
            ((XsdNodeRetriever) nodeRetriever).setTreePopulator(treePopulator);
        } else {
            this.nodeRetriever = new XmlNodeRetriever(xsdFilepath, getCurrentLoopXPath());
        }
    }

    public String getAbsoluteXPath(TreeItem treeItem) {
        return treePopulator.getAbsoluteXPath(treeItem);
    }

    /**
     * DOC amaumont Comment method "init".
     * 
     * @param tree
     */
    private void init() {

        Display display = getBgDrawableComposite().getDisplay();

        initColors(display);

        StyleLink unselectedStyleLink = new StyleLink();
        unselectedStyleLink.setDrawableLink(new BezierHorizontalLink(unselectedStyleLink));
        unselectedStyleLink.setForegroundColor(display.getSystemColor(SWT.COLOR_GRAY));
        unselectedStyleLink.setLineWidth(2);

        int xOffset = WindowSystem.isGTK() ? 2 : -2;
        int yOffset = WindowSystem.isGTK() ? -1 : 0;
        unselectedStyleLink.setExtremity2(new ExtremityEastArrow(unselectedStyleLink, -ExtremityEastArrow.WIDTH_ARROW + xOffset,
                yOffset));
        setUnselectedStyleLink(unselectedStyleLink);

        getSelectedRelativeStyleLink();

        initListeners();
        createLinks();

    }

    /**
     * DOC amaumont Comment method "initColors".
     * 
     * @param display
     */
    private void initColors(Display display) {
        selectedLoopLinkColor = new Color(display, 255, 131, 0);
        selectedRelativeLinkColor = new Color(display, 110, 168, 255);
        getTree().addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                selectedLoopLinkColor.dispose();
                selectedRelativeLinkColor.dispose();
                getTree().removeDisposeListener(this);
            }

        });

    }

    /**
     * DOC amaumont Comment method "initListeners".
     */
    private void initListeners() {
        initLoopListeners();
        initFieldsListeners();
        new MDMToSchemaDragAndDropHandler(this);
    }

    /**
     * DOC amaumont Comment method "createLinks".
     */
    public void createLinks() {

        removeAllLinks();
        getBackgroundRefresher().refreshBackground();

        ProgressDialog progressDialog = new ProgressDialog(getTree().getShell(), 1000) {

            private IProgressMonitor monitorWrap;

            @Override
            public void run(IProgressMonitor monitor) {

                TableItem[] loopTableItems = loopTableEditorView.getTable().getItems();
                TableItem[] fieldsTableItems = fieldsTableEditorView.getTable().getItems();

                monitorWrap = new EventLoopProgressMonitor(monitor);

                String taskName = Messages.getString("XmlToXPathLinker.Loop"); //$NON-NLS-1$
                int totalWork = loopTableItems.length + fieldsTableItems.length;

                monitorWrap.beginTask(taskName, totalWork);

                List<Concept> conceptList = loopTableEditorView.getModel().getBeansList();
                for (int i = 0; i < loopTableItems.length; i++) {

                    if (monitorWrap.isCanceled()) {
                        return;
                    }

                    TableItem tableItem = loopTableItems[i];
                    Concept concept = conceptList.get(i);
                    ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(concept.getConnection(), true);
                    String originalValue = concept.getLoopExpression();
                    if (contextType != null) {
                        originalValue = ConnectionContextHelper.getOriginalValue(contextType, concept.getLoopExpression());
                        originalValue = TalendQuoteUtils.removeQuotes(originalValue);
                    }
                    if (originalValue != null) {
                        createLoopLinks(originalValue, tableItem, monitorWrap);
                    }

                    monitorWrap.worked(1);
                }

                List<ConceptTarget> schemaTargetList = fieldsTableEditorView.getModel().getBeansList();

                createFieldsLinkWithProgressMonitor(monitorWrap, schemaTargetList.size() + loopTableItems.length,
                        schemaTargetList, 0, fieldsTableItems.length);

                monitorWrap.done();

            }

        };

        try {
            progressDialog.executeProcess();
        } catch (InvocationTargetException e) {
            ExceptionHandler.process(e);
        } catch (InterruptedException e) {
            // Nothing to do
        }

    }

    /**
     * DOC amaumont Comment method "createFieldsLinkWithProgressDialog".
     * 
     * @param monitorWrap
     * @param fieldsTableItems
     * @param totalWork
     * @param schemaTargetList
     */
    private void createFieldsLinkWithProgressMonitor(IProgressMonitor monitorWrap, int totalWork,
            List<ConceptTarget> schemaTargetList, int startTableItem, int tableItemLength) {
        monitorWrap.beginTask(Messages.getString("XmlToXPathLinker.beginTask.fieldLinksCreation"), totalWork); //$NON-NLS-1$

        TableItem[] fieldsTableItems = fieldsTableEditorView.getTable().getItems();

        for (int i = startTableItem, indexShemaTarget = 0; i < startTableItem + tableItemLength; i++, indexShemaTarget++) {

            if (monitorWrap.isCanceled()) {
                return;
            }

            TableItem tableItem = fieldsTableItems[i];
            ConceptTarget schemaTarget = schemaTargetList.get(indexShemaTarget);
            String relativeXpathQuery = schemaTarget.getRelativeLoopExpression();
            createFieldLinks(relativeXpathQuery, tableItem, monitorWrap);

            monitorWrap.worked(1);
        }
    }

    /**
     * DOC amaumont Comment method "initListeners".
     */
    private void initLoopListeners() {

        MDMXSDExtractorLoopModel loopModel = this.loopTableEditorView.getModel();

        final Table loopTable = this.loopTableEditorView.getTableViewerCreator().getTable();

        loopModel.addModifiedBeanListener(new IModifiedBeanListener<Concept>() {

            @Override
            public void handleEvent(ModifiedBeanEvent<Concept> event) {
                handleModifiedBeanEvent(event);
            }

            private void handleModifiedBeanEvent(ModifiedBeanEvent<Concept> event) {
                if (event.column == loopTableEditorView.getXPathColumn()) {
                    onXPathValueChanged(loopTable, (String) event.newValue, event.index);
                }

            }

        });

        this.loopTableEditorView.getExtendedTableViewer().addListener(new IExtendedControlListener() {

            @Override
            public void handleEvent(ExtendedControlEvent event) {
                if (event.getType() == EVENT_TYPE.MODEL_CHANGED) {
                    nodeRetriever.setCurrentLoopXPath(getCurrentLoopXPath());
                }
            }

        });

        SelectionHelper selectionHelper = this.loopTableEditorView.getTableViewerCreator().getSelectionHelper();
        final ILineSelectionListener afterLineSelectionListener = new ILineSelectionListener() {

            @Override
            public void handle(LineSelectionEvent e) {
                updateLinksStyleAndControlsSelection(e.source.getTable(), true);
            }
        };
        selectionHelper.addAfterSelectionListener(afterLineSelectionListener);
    }

    /**
     * DOC amaumont Comment method "initListeners".
     */
    private void initFieldsListeners() {

        MDMXSDExtractorFieldModel schemaModel = this.fieldsTableEditorView.getModel();

        final Table fieldsTable = this.fieldsTableEditorView.getTable();

        schemaModel.addModifiedBeanListener(new IModifiedBeanListener<ConceptTarget>() {

            @Override
            public void handleEvent(ModifiedBeanEvent<ConceptTarget> event) {
                handleModifiedBeanEvent(event);
            }

            private void handleModifiedBeanEvent(ModifiedBeanEvent<ConceptTarget> event) {
                if (event.column == fieldsTableEditorView.getXPathColumn()) {
                    onXPathValueChanged(fieldsTable, (String) event.newValue, event.index);
                }

            }

        });

        schemaModel.addBeforeOperationListListener(-50, new IListenableListListener<ConceptTarget>() {

            @Override
            public void handleEvent(ListenableListEvent<ConceptTarget> event) {
                handleListenableListBeforeTableViewerRefreshedEvent(event);
            }

        });

        schemaModel.addAfterOperationListListener(new IListenableListListener<ConceptTarget>() {

            @Override
            public void handleEvent(ListenableListEvent<ConceptTarget> event) {
                handleListenableListAfterTableViewerRefreshedEvent(event);
            }

        });

        SelectionHelper selectionHelper = this.fieldsTableEditorView.getTableViewerCreator().getSelectionHelper();
        final ILineSelectionListener afterLineSelectionListener = new ILineSelectionListener() {

            @Override
            public void handle(LineSelectionEvent e) {
                updateLinksStyleAndControlsSelection(e.source.getTable(), true);
            }
        };
        selectionHelper.addAfterSelectionListener(afterLineSelectionListener);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.commons.ui.swt.linking.TreeToTableLinker#drawBackground(org.eclipse.swt.graphics.GC)
     */
    @Override
    public void drawBackground(GC gc) {

        Rectangle clipBounds = getTree().getBounds();

        List<Table> tables = getTables();

        if (tables == null || tables.size() == 0) {
            throw new IllegalStateException();
        }
        Table table = tables.get(0);

        Rectangle tableBounds = table.getDisplay().map(table, getBgDrawableComposite(), table.getBounds());
        // System.out.println(tableBounds);

        // System.out.println(getBgDrawableComposite());

        int offset = WindowSystem.isGTK() ? 0 : 20;

        clipBounds.width = tableBounds.x;
        clipBounds.height += offset - 4;
        clipBounds.x = 0;
        clipBounds.y = offset;

        gc.setClipping(clipBounds);

        super.drawBackground(gc);

        gc.setClipping((Rectangle) null);
    }

    private void handleListenableListBeforeTableViewerRefreshedEvent(ListenableListEvent<ConceptTarget> event) {
        if (event.type == TYPE.REMOVED) {
            Collection<ConceptTarget> removedObjects = event.removedObjects;
            for (ConceptTarget target : removedObjects) {
                linksManager.removeLinksFromDataItem2(target);
            }
        }

    }

    public void handleListenableListAfterTableViewerRefreshedEvent(final ListenableListEvent<ConceptTarget> event) {
        if (event.type == ListenableListEvent.TYPE.ADDED) {

            ProgressDialog progressDialog = new ProgressDialog(getTree().getShell(), 1000) {

                private IProgressMonitor monitorWrap;

                @Override
                public void run(IProgressMonitor monitor) {

                    monitorWrap = new EventLoopProgressMonitor(monitor);

                    List<ConceptTarget> addedObjects = new ArrayList<ConceptTarget>(event.addedObjects);

                    MDMLinker.this.createFieldsLinkWithProgressMonitor(monitorWrap, addedObjects.size(), addedObjects,
                            event.index, addedObjects.size());

                    monitorWrap.done();

                }

            };

            try {
                progressDialog.executeProcess();
            } catch (InvocationTargetException e) {
                ExceptionHandler.process(e);
            } catch (InterruptedException e) {
                // Nothing to do
            }

            getBackgroundRefresher().refreshBackground();
        } else if (event.type == ListenableListEvent.TYPE.SWAPED) {
            getBackgroundRefresher().refreshBackground();
        } else if (event.type == TYPE.REMOVED) {
            getBackgroundRefresher().refreshBackground();
        }

    }

    /**
     * DOC amaumont Comment method "addLink".
     * 
     * @param treeItem
     * @param dataItem1
     * @param table
     * @param dataItem2
     */
    public void addLoopLink(TreeItem treeItem, Object dataItem1, Table table, Concept dataItem2) {
        LinkDescriptor<TreeItem, Object, Table, Object> link = addLink(treeItem, dataItem1, table, dataItem2);
        link.setStyleLink(getSelectedLoopStyleLink());
    }

    /**
     * DOC amaumont Comment method "addLink".
     * 
     * @param treeItem
     * @param dataItem1
     * @param table
     * @param dataItem2
     */
    private LinkDescriptor<TreeItem, Object, Table, Object> addLink(TreeItem treeItem, Object dataItem1, Table table,
            Object dataItem2) {
        LinkDescriptor<TreeItem, Object, Table, Object> link = new LinkDescriptor<TreeItem, Object, Table, Object>(
                new TreeExtremityDescriptor(treeItem, dataItem1), new ExtremityLink<Table, Object>(table, dataItem2));

        link.setStyleLink(getUnselectedStyleLink());
        getLinksManager().addLink(link);
        updateLinksStyleAndControlsSelection(table, true);
        return link;
    }

    /**
     * DOC amaumont Comment method "addLink".
     * 
     * @param treeItem
     * @param dataItem1
     * @param table
     * @param dataItem2
     */
    public void addFieldLink(TreeItem treeItem, Object dataItem1, Table table, ConceptTarget dataItem2) {
        LinkDescriptor<TreeItem, Object, Table, Object> link = addLink(treeItem, dataItem1, table, dataItem2);
        // link.setStyleLink(getSelectedStyleLink());
    }

    /**
     * DOC amaumont Comment method "removeAllLinks".
     */
    public void removeAllLinks() {
        getLinksManager().clearLinks();
    }

    public boolean removeLinksFromTableEntry(ConceptTarget schemaTarget) {
        return getLinksManager().removeLinksFromDataItem2(schemaTarget);
    }

    /**
     * DOC amaumont Comment method "onXPathValueChanged".
     * 
     * @param table
     * @param newValue
     * @param itemIndex
     */
    public void onXPathValueChanged(Table table, String newValue, int itemIndex) {
        if (isLoopTable(table)) {
            nodeRetriever.setCurrentLoopXPath(newValue);
        }
        TableItem tableItem = table.getItem(itemIndex);
        linksManager.removeLinksFromDataItem2(tableItem.getData());
        if (isLoopTable(table)) {
            createLinks();
        } else {
            createFieldLinks(newValue, tableItem, null);
        }
        getBackgroundRefresher().refreshBackground();

    }

    /**
     * DOC amaumont Comment method "isLoopTable".
     * 
     * @param table
     * @return
     */
    public boolean isLoopTable(Table table) {
        return loopTableEditorView.getTableViewerCreator().getTable() == table;
    }

    /**
     * DOC amaumont Comment method "createLoopLinks".
     * 
     * @param monitorWrap
     * 
     * @param pathQuery
     * @param tableItem
     */
    private void createLoopLinks(String xPathExpression, TableItem tableItemTarget, IProgressMonitor monitorWrap) {

        if (xPathExpression == null || xPathExpression.trim().length() == 0) {
            loopXpathNodes.clear();
            uniqueLoopNodes.clear();
            allLoopNodes.clear();
            return;
        }

        Set<String> alreadyProcessedXPath = new HashSet<String>();
        List<Node> nodeList = null;

        loopXpathNodes = new ArrayList<String>();
        uniqueLoopNodes = new ArrayList<Node>();
        allLoopNodes = new ArrayList<Node>();
        if (!isXSDFile) {
            try {
                nodeList = this.nodeRetriever.retrieveListOfNodes(xPathExpression);
            } catch (XPathExpressionException e) {
                ExceptionHandler.process(e);
            }
            if (nodeList != null) {
                for (Node node : nodeList) {

                    if (monitorWrap != null && monitorWrap.isCanceled()) {
                        break;
                    }

                    allLoopNodes.add(node);
                    String absoluteXPathFromNode = nodeRetriever.getAbsoluteXPathFromNode(node);
                    if (!alreadyProcessedXPath.contains(absoluteXPathFromNode)) {
                        TreeItem treeItemFromAbsoluteXPath = treePopulator.getTreeItem(absoluteXPathFromNode);
                        if (treeItemFromAbsoluteXPath != null) {
                            loopXpathNodes.add(absoluteXPathFromNode);
                            uniqueLoopNodes.add(node);
                            addLoopLink(treeItemFromAbsoluteXPath, treeItemFromAbsoluteXPath.getData(),
                                    tableItemTarget.getParent(), (Concept) tableItemTarget.getData());
                            alreadyProcessedXPath.add(absoluteXPathFromNode);
                        }
                    }
                }
            }
        } else {
            if (!alreadyProcessedXPath.contains(xPathExpression)) {
                loopXpathNodes.add(xPathExpression);
                TreeItem treeItemFromAbsoluteXPath = treePopulator.getTreeItem(xPathExpression);
                if (treeItemFromAbsoluteXPath != null) {
                    addLoopLink(treeItemFromAbsoluteXPath, treeItemFromAbsoluteXPath.getData(), tableItemTarget.getParent(),
                            (Concept) tableItemTarget.getData());
                    alreadyProcessedXPath.add(xPathExpression);
                }
            }
        }
    }

    /**
     * DOC amaumont Comment method "addLinks".
     * 
     * @param relativeXpath
     * @param tableItemTarget
     * @param progressMonitor
     * @throws XPathExpressionException
     */
    private void createFieldLinks(final String relativeXpathPrm, final TableItem tableItemTarget, IProgressMonitor progressMonitor) {

        if (relativeXpathPrm == null || relativeXpathPrm.trim().length() == 0) {
            return;
        }

        boolean breakAll = false;

        boolean expressionIsAbsolute = false;
        if (relativeXpathPrm.trim().startsWith("/")) { //$NON-NLS-1$
            expressionIsAbsolute = true;
        }

        String relativeXpath = relativeXpathPrm;
        boolean limitLoopExceeded = false;
        boolean limitFieldExceeded = false;

        Set<String> alreadyProcessedXPath = new HashSet<String>();

        if (!isXSDFile) {
            if (!expressionIsAbsolute) {

                int nodesLoopMax = 50;
                int nodesFieldMax = 50;

                int lstSize = allLoopNodes.size();

                int loopNumber = lstSize;

                if (lstSize > nodesLoopMax) {
                    loopNumber = nodesLoopMax;
                    limitLoopExceeded = true;
                }

                // System.out.println("lstSize=" + lstSize);
                for (int i = 0; i < loopNumber; i++) {

                    if (progressMonitor != null && progressMonitor.isCanceled()) {
                        break;
                    }

                    // System.out.println("main node index : " + i);
                    Node loopNode = allLoopNodes.get(i);

                    List<Node> relativeNodeList = null;
                    try {
                        // TimeMeasure.start("relative");
                        relativeNodeList = this.nodeRetriever.retrieveNodeListFromNode(relativeXpath, loopNode);
                        // if (false) {
                        // break;
                        // }
                    } catch (XPathExpressionException e) {
                        continue;
                    } finally {
                        // TimeMeasure.end("relative");
                    }

                    int length = relativeNodeList.size();

                    if (length > nodesFieldMax) {
                        length = nodesFieldMax;
                        limitFieldExceeded = true;
                    }

                    for (int j = 0; j < length; j++) {

                        if (progressMonitor != null && progressMonitor.isCanceled()) {
                            breakAll = true;
                            break;
                        }

                        Node relativeNode = relativeNodeList.get(j);

                        if (relativeNode != null) {
                            String absoluteXPathFromNode = nodeRetriever.getAbsoluteXPathFromNode(relativeNode);
                            if (!alreadyProcessedXPath.contains(absoluteXPathFromNode)) {
                                TreeItem treeItemFromAbsoluteXPath = treePopulator.getTreeItem(absoluteXPathFromNode);
                                if (treeItemFromAbsoluteXPath != null) {
                                    addFieldLink(treeItemFromAbsoluteXPath, treeItemFromAbsoluteXPath.getData(),
                                            tableItemTarget.getParent(), (ConceptTarget) tableItemTarget.getData());
                                    alreadyProcessedXPath.add(absoluteXPathFromNode);
                                }
                            }
                        }
                    }

                    if (limitFieldExceeded || breakAll) {
                        break;
                    }

                }
            }

            if (limitLoopExceeded || limitFieldExceeded || expressionIsAbsolute) {

                String expression = null;

                if (relativeXpath == null) {
                    relativeXpath = ""; //$NON-NLS-1$
                }

                String currentLoopXPath = getCurrentLoopXPath();

                if (relativeXpath.trim().startsWith("/")) { //$NON-NLS-1$
                    expression = relativeXpath;
                } else {
                    expression = currentLoopXPath + "/" + relativeXpath; //$NON-NLS-1$
                }

                List<Node> nodeList = null;
                try {
                    nodeList = this.nodeRetriever.retrieveNodeList(expression);
                } catch (XPathExpressionException e) {
                    ExceptionHandler.process(e);
                }

                if (nodeList != null) {
                    int length = nodeList.size();
                    for (int i = 0; i < length; i++) {

                        if (progressMonitor != null && progressMonitor.isCanceled()) {
                            break;
                        }

                        // System.out.println("main node index : " + i);
                        Node loopNode = nodeList.get(i);
                        String absoluteXPathFromNode = nodeRetriever.getAbsoluteXPathFromNode(loopNode);
                        if (!alreadyProcessedXPath.contains(absoluteXPathFromNode)) {
                            TreeItem treeItemFromAbsoluteXPath = treePopulator.getTreeItem(absoluteXPathFromNode);
                            if (treeItemFromAbsoluteXPath != null) {
                                addFieldLink(treeItemFromAbsoluteXPath, treeItemFromAbsoluteXPath.getData(),
                                        tableItemTarget.getParent(), (ConceptTarget) tableItemTarget.getData());
                                alreadyProcessedXPath.add(absoluteXPathFromNode);
                            }
                        }
                    }
                }

            }
        } else {
            String fullPath = ""; //$NON-NLS-1$
            if (!expressionIsAbsolute) {
                if (loopXpathNodes.size() > 0) {
                    fullPath = loopXpathNodes.get(0) + "/"; //$NON-NLS-1$
                }
                // adapt relative path
                String[] relatedSplitedPaths = relativeXpath.split("\\.\\./"); //$NON-NLS-1$
                if (relatedSplitedPaths.length > 1) {
                    int pathsToRemove = relatedSplitedPaths.length - 1;
                    String[] fullPathSplited = fullPath.split("/"); //$NON-NLS-1$
                    fullPath = ""; //$NON-NLS-1$
                    for (int i = 1; i < (fullPathSplited.length - pathsToRemove); i++) {
                        fullPath += "/" + fullPathSplited[i]; //$NON-NLS-1$
                    }
                    fullPath += "/" + relatedSplitedPaths[pathsToRemove]; //$NON-NLS-1$
                } else {
                    fullPath += relativeXpath;
                }
            } else {
                fullPath = relativeXpath;
            }
            TreeItem treeItemFromAbsoluteXPath = treePopulator.getTreeItem(fullPath);
            if (treeItemFromAbsoluteXPath != null && !alreadyProcessedXPath.contains(fullPath)) {
                addFieldLink(treeItemFromAbsoluteXPath, treeItemFromAbsoluteXPath.getData(), tableItemTarget.getParent(),
                        (ConceptTarget) tableItemTarget.getData());
                alreadyProcessedXPath.add(fullPath);
            }
        }

    }

    public ConceptTarget getNewSchemaTargetEntry(String relativeXPathValue) {

        MDMXSDExtractorFieldModel xpathNodeSchemaModel = fieldsTableEditorView.getModel();
        ConceptTarget schemaTarget = xpathNodeSchemaModel.createNewSchemaTarget();
        schemaTarget.setRelativeLoopExpression(relativeXPathValue);

        return schemaTarget;
    }

    /**
     * Getter for tableEditorView.
     * 
     * @return the tableEditorView
     */
    public ExtractionFieldsWithMDMEditorView getFieldsTableEditorView() {
        return this.fieldsTableEditorView;
    }

    /**
     * Getter for loopTableEditorView.
     * 
     * @return the loopTableEditorView
     */
    public ExtractionLoopWithMDMEditorView getLoopTableEditorView() {
        return this.loopTableEditorView;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void updateLinksStyleAndControlsSelection(Control currentControl, Boolean flag) {

        boolean selectedControlIsTable = false;
        if (currentControl instanceof Table) {
            selectedControlIsTable = true;
        } else if (currentControl instanceof Tree) {
            selectedControlIsTable = false;
        } else {
            throw new IllegalArgumentException(Messages.getString("XmlToXPathLinker.illegalArgumentException")); //$NON-NLS-1$
        }

        HashSet selectedItems = new HashSet();
        Map itemsToSelect = new HashMap();

        if (selectedControlIsTable) {
            for (Table table : getTables()) {
                if (table != currentControl) {
                    table.deselectAll();
                    if (isFieldsTable(table)) {
                        fieldsTableEditorView.getExtendedToolbar().updateEnabledStateOfButtons();
                    }
                }
            }

            TableItem[] selection = ((Table) currentControl).getSelection();
            for (TableItem tableItem : selection) {
                selectedItems.add(tableItem.getData());
            }

        } else {

            TreeItem[] selection = getTree().getSelection();
            for (TreeItem treeItem : selection) {
                selectedItems.add(treeItem.getData());
            }

        }

        List<LinkDescriptor<TreeItem, Object, Table, Object>> links = linksManager.getLinks();
        for (LinkDescriptor<TreeItem, Object, Table, Object> link : links) {
            IStyleLink styleLink = null;
            IExtremityLink extremity = null;
            IExtremityLink otherExtremity = null;
            if (selectedControlIsTable) {
                extremity = link.getExtremity2();
                otherExtremity = link.getExtremity1();
            } else {
                extremity = link.getExtremity1();
                otherExtremity = link.getExtremity2();
            }
            boolean currentItemIsSelected = selectedItems.contains(extremity.getDataItem());

            if (extremity.getGraphicalObject() == loopTableEditorView.getTableViewerCreator().getTable()
                    || otherExtremity.getGraphicalObject() == loopTableEditorView.getTableViewerCreator().getTable()) {
                styleLink = getSelectedLoopStyleLink();
            } else {

                if (currentItemIsSelected) {
                    styleLink = getSelectedStyleLink();
                    if (selectedControlIsTable) {

                        itemsToSelect.put(otherExtremity.getGraphicalObject(), null);

                    } else {

                        Table currentTable = (Table) otherExtremity.getGraphicalObject();
                        List<TableItem> tableItemsToSelect = (List<TableItem>) itemsToSelect.get(currentTable);

                        if (tableItemsToSelect == null) {
                            tableItemsToSelect = new ArrayList<TableItem>();
                            itemsToSelect.put(currentTable, tableItemsToSelect);
                        }
                        TableItem tableItem = TableUtils.getTableItem(currentTable, otherExtremity.getDataItem());
                        tableItemsToSelect.add(tableItem);
                    }
                } else {
                    styleLink = getUnselectedStyleLink();
                }
            }
            if (styleLink == null) {
                styleLink = getDefaultStyleLink();
            }
            link.setStyleLink(styleLink);

        }
        if (selectedControlIsTable) {
            (getTree()).setSelection((TreeItem[]) itemsToSelect.keySet().toArray(new TreeItem[0]));
        } else {
            Set<Table> set = itemsToSelect.keySet();
            if (set.size() > 0) {
                for (Table table : set) {
                    ArrayList<TableItem> tableItemsToSelect = (ArrayList<TableItem>) itemsToSelect.get(table);
                    table.deselectAll();
                    TableItem[] tableItems = tableItemsToSelect.toArray(new TableItem[0]);
                    table.setSelection(tableItems);
                }
            } else {
                loopTableEditorView.getTable().deselectAll();
                fieldsTableEditorView.getTable().deselectAll();
            }
            fieldsTableEditorView.getExtendedToolbar().updateEnabledStateOfButtons();
        }
        getLinksManager().sortLinks(getDrawingLinksComparator());
        getBackgroundRefresher().refreshBackground();
    }

    /**
     * DOC amaumont Comment method "isFieldsTable".
     * 
     * @param table
     * @return
     */
    public boolean isFieldsTable(Table table) {
        return fieldsTableEditorView.getTableViewerCreator().getTable() == table;
    }

    public StyleLink getSelectedLoopStyleLink() {
        if (this.selectedLoopStyleLink == null) {
            StyleLink styleLink = new StyleLink();
            styleLink.setDrawableLink(new BezierHorizontalLink(styleLink));
            styleLink.setForegroundColor(selectedLoopLinkColor);
            styleLink.setLineWidth(2);
            int xOffset = WindowSystem.isGTK() ? 2 : -2;
            int yOffset = WindowSystem.isGTK() ? -1 : 0;
            styleLink.setExtremity2(new ExtremityEastArrow(styleLink, -ExtremityEastArrow.WIDTH_ARROW + xOffset, yOffset));
            this.selectedLoopStyleLink = styleLink;
        }
        return this.selectedLoopStyleLink;
    }

    /**
     * DOC amaumont Comment method "getSelectedRelativeStyleLink".
     * 
     * @param selectedLoopLinkColor
     */
    private void getSelectedRelativeStyleLink() {

        StyleLink selectedStyleLink = new StyleLink();
        selectedStyleLink.setDrawableLink(new BezierHorizontalLink(selectedStyleLink));
        selectedStyleLink.setForegroundColor(selectedRelativeLinkColor);
        selectedStyleLink.setLineWidth(2);
        int xOffset = WindowSystem.isGTK() ? 2 : -2;
        int yOffset = WindowSystem.isGTK() ? -1 : 0;
        selectedStyleLink.setExtremity2(new ExtremityEastArrow(selectedStyleLink, -ExtremityEastArrow.WIDTH_ARROW + xOffset,
                yOffset));
        setSelectedStyleLink(selectedStyleLink);
    }

    public String getCurrentLoopXPath() {
        return loopTableEditorView.getExtendedTableModel().getBeansList().get(0).getLoopExpression();
    }

    public void parseAllFieldsXPathExpressions() {

        List<ConceptTarget> beansList = fieldsTableEditorView.getExtendedTableModel().getBeansList();

        int lstSize = beansList.size();
        for (int i = 0; i < lstSize; i++) {
            ConceptTarget schemaTarget = beansList.get(i);
            onXPathValueChanged(fieldsTableEditorView.getTable(), schemaTarget.getRelativeLoopExpression(), i);
        }

    }

    /**
     * Getter for loopXpathNodes.
     * 
     * @return the loopXpathNodes
     */
    public ArrayList<String> getLoopXpathNodes() {
        return this.loopXpathNodes;
    }

    /**
     * DOC amaumont Comment method "validateXPathExpression".
     * 
     * @param newValue
     * @return null if expression is valid, else return the error message.
     */
    public String validateXPathExpression(String xpathExpression) {
        if (xpathExpression == null || xpathExpression.trim().length() == 0) {
            return null;
        }
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();
        try {
            xpath.compile(xpathExpression);
        } catch (Exception e) {
            return Messages.getString("XmlToXPathLinker.exceptionReturn.xPathInvalid"); //$NON-NLS-1$
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.commons.ui.swt.linking.TreeToTablesLinker#getDrawingLinksComparator()
     */
    @Override
    protected Comparator<LinkDescriptor<TreeItem, Object, Table, Object>> getDrawingLinksComparator() {
        if (this.drawingLinksComparator == null) {
            this.drawingLinksComparator = new Comparator<LinkDescriptor<TreeItem, Object, Table, Object>>() {

                @Override
                public int compare(LinkDescriptor<TreeItem, Object, Table, Object> link1,
                        LinkDescriptor<TreeItem, Object, Table, Object> link2) {
                    IStyleLink link1StyleLink = link1.getStyleLink();
                    IStyleLink link2StyleLink = link2.getStyleLink();
                    if (link1StyleLink == link2StyleLink) {
                        return 0;
                    }
                    if (link1StyleLink == getSelectedLoopStyleLink()) {
                        return 1;
                    }
                    if (link2StyleLink == getSelectedLoopStyleLink()) {
                        return -1;
                    }
                    if (link1StyleLink == getUnselectedStyleLink()) {
                        return -1;
                    }
                    return 1;
                }

            };
        }
        return this.drawingLinksComparator;
    }

    /**
     * Getter for allLoopNodes.
     * 
     * @return the allLoopNodes
     */
    public ArrayList<Node> getAllLoopNodes() {
        return this.allLoopNodes;
    }

    /**
     * Getter for uniqueLoopNodes.
     * 
     * @return the uniqueLoopNodes
     */
    public ArrayList<Node> getUniqueLoopNodes() {
        return this.uniqueLoopNodes;
    }

    /**
     * Getter for nodeRetriever.
     * 
     * @return the nodeRetriever
     */
    public XmlNodeRetriever getNodeRetriever() {
        return this.nodeRetriever;
    }

}
