package com.databasepreservation.dbviewer.client.browse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.roda.core.data.adapter.filter.EmptyKeyFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.FilterParameter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.IsIndexed;

import com.databasepreservation.dbviewer.client.BrowserService;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerCell;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerColumn;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerForeignKey;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerReference;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerRow;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerSchema;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerTable;
import com.databasepreservation.dbviewer.client.common.search.SearchPanel;
import com.databasepreservation.dbviewer.client.common.search.TableSearchPanel;
import com.databasepreservation.dbviewer.client.common.sidebar.DatabaseSidebar;
import com.databasepreservation.dbviewer.client.main.BreadcrumbPanel;
import com.databasepreservation.dbviewer.shared.client.Tools.BreadcrumbManager;
import com.databasepreservation.dbviewer.shared.client.Tools.HistoryManager;
import com.databasepreservation.dbviewer.shared.client.Tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ReferencesPanel extends Composite {
  private static Map<String, ReferencesPanel> instances = new HashMap<>();

  public static ReferencesPanel getInstance(String databaseUUID, String tableUUID, String recordUUID,
    String columnIndexInTable) {
    return new ReferencesPanel(databaseUUID, tableUUID, recordUUID, columnIndexInTable);
  }

  interface DatabasePanelUiBinder extends UiBinder<Widget, ReferencesPanel> {
  }

  private static DatabasePanelUiBinder uiBinder = GWT.create(DatabasePanelUiBinder.class);

  private ViewerDatabase database;
  private ViewerTable table;
  private final String recordUUID;
  private ViewerRow record;
  private Integer columnIndexInTable;

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField(provided = true)
  SearchPanel dbSearchPanel;

  @UiField(provided = true)
  DatabaseSidebar sidebar;

  @UiField
  FlowPanel content;

  @UiField
  Label mainHeader;

  @UiField
  Label cellSchema;
  @UiField
  Label cellTable;
  @UiField
  Label cellColumn;

  private ReferencesPanel(final String databaseUUID, final String tableUUID, final String recordUUID,
    final String columnIndexInTable) {
    this.recordUUID = recordUUID;
    this.columnIndexInTable = Integer.valueOf(columnIndexInTable);
    dbSearchPanel = new SearchPanel(new Filter(), "", "Search in all tables", false, false);
    sidebar = DatabaseSidebar.getInstance(databaseUUID);

    initWidget(uiBinder.createAndBindUi(this));

    BreadcrumbManager.updateBreadcrumb(breadcrumb,
      BreadcrumbManager.loadingReferences(databaseUUID, tableUUID, recordUUID, columnIndexInTable));

    BrowserService.Util.getInstance().retrieve(ViewerDatabase.class.getName(), databaseUUID,
      new AsyncCallback<IsIndexed>() {
        @Override
        public void onFailure(Throwable caught) {
          throw new RuntimeException(caught);
        }

        @Override
        public void onSuccess(IsIndexed result) {
          database = (ViewerDatabase) result;
          table = database.getMetadata().getTable(tableUUID);
          init();
        }
      });

    BrowserService.Util.getInstance().retrieveRows(ViewerRow.class.getName(), tableUUID, recordUUID,
      new AsyncCallback<IsIndexed>() {
        @Override
        public void onFailure(Throwable caught) {
          throw new RuntimeException(caught);
        }

        @Override
        public void onSuccess(IsIndexed result) {
          record = (ViewerRow) result;
          init();
        }
      });
  }

  private void init() {
    if (database == null) {
      return;
    }

    String columnName = "<unknown>";
    if (columnIndexInTable >= 0 && columnIndexInTable < table.getColumns().size()) {
      columnName = table.getColumns().get(columnIndexInTable).getDisplayName();
    }

    cellTable.setText(table.getSchemaName());
    cellSchema.setText(table.getName());
    cellColumn.setText(columnName);

    // breadcrumb
    BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.forReferences(database.getMetadata().getName(),
      database.getUUID(), table.getSchemaName(), table.getSchemaUUID(), table.getName(), table.getUUID(), recordUUID,
      columnName, columnIndexInTable.toString()));

    if (record != null) {
      // update title
      String value = "NULL";
      ViewerCell cell = record.getCells().get(table.getColumns().get(columnIndexInTable).getSolrName());
      if (cell != null && ViewerStringUtils.isNotBlank(cell.getValue())) {
        value = cell.getValue();
      }
      mainHeader.setText("References for value \"" + value + "\"");

      TreeMap<Reference, TableSearchPanel> references = new TreeMap<>();

      // get references where this column is source in foreign keys
      // for (ViewerForeignKey fk : table.getForeignKeys()) {
      // for (ViewerReference viewerReference : fk.getReferences()) {
      // if (viewerReference.getSourceColumnIndex().equals(columnIndexInTable))
      // {
      // Reference reference = new Reference(table, fk);
      // references.put(reference, createTableRowListFromReference(reference));
      // break;
      // }
      // }
      // }

      // get references where this column is (at least one of) the target of
      // foreign keys or is (at least one of) the sources of the foreign keys
      for (ViewerSchema viewerSchema : database.getMetadata().getSchemas()) {
        for (ViewerTable viewerTable : viewerSchema.getTables()) {
          for (ViewerForeignKey viewerForeignKey : viewerTable.getForeignKeys()) {

            boolean fkSourceIsCurrentTable = viewerTable.equals(table);
            boolean fkTargetIsCurrentTable = viewerForeignKey.getReferencedTableUUID().equals(table.getUUID());

            // the table that is different than the current table, to use in the
            // reference
            ViewerTable otherTable;
            if (fkSourceIsCurrentTable && fkTargetIsCurrentTable) {
              // cyclic foreign key
              otherTable = table;
            } else if (fkSourceIsCurrentTable) {
              otherTable = database.getMetadata().getTable(viewerForeignKey.getReferencedTableUUID());
            } else if (fkTargetIsCurrentTable) {
              otherTable = viewerTable;
            } else {
              // if the current table is not target nor source for the foreign
              // key, skip the foreign key
              continue;
            }

            for (ViewerReference viewerReference : viewerForeignKey.getReferences()) {
              int columnIndexOnCurrentTable;
              if (fkSourceIsCurrentTable) {
                columnIndexOnCurrentTable = viewerReference.getSourceColumnIndex();
              } else {
                // fkTargetIsCurrentTable equals true
                columnIndexOnCurrentTable = viewerReference.getReferencedColumnIndex();
              }

              if (columnIndexOnCurrentTable == columnIndexInTable) {
                Reference reference = new Reference(otherTable, viewerForeignKey);
                if (!references.containsKey(reference)) {
                  references.put(reference, createTableSearchPanelFromReference(reference));
                  break; // its selected. avoid checking more columns
                }
              }
            }
          }
        }
      }

      for (Map.Entry<Reference, TableSearchPanel> entry : references.entrySet()) {
        addReferenceTable(entry.getKey(), entry.getValue());
      }
    }
  }

  private void addReferenceTable(Reference reference, TableSearchPanel table) {
    content.add(getReferenceHeaderPanel(reference));
    content.add(table);
  }

  private Widget getReferenceHeaderPanel(Reference reference) {
    ViewerTable otherTable = reference.table;
    ViewerForeignKey fk = reference.foreignKey;
    boolean currentTableIsReferencedTableInForeignKey = fk.getReferencedTableUUID().equals(table.getUUID());

    FlowPanel header = new FlowPanel();
    header.addStyleName("field");

    SafeHtmlBuilder relationNameBuilder = new SafeHtmlBuilder();
    relationNameBuilder.appendHtmlConstant("Relation: ");
    // relation source
    if (currentTableIsReferencedTableInForeignKey) {
      relationNameBuilder.appendEscaped(otherTable.getName()).appendHtmlConstant(" (");
      for (Iterator<ViewerReference> i = fk.getReferences().iterator(); i.hasNext();) {
        ViewerReference viewerReference = i.next();
        relationNameBuilder.appendEscaped(otherTable.getColumns().get(viewerReference.getSourceColumnIndex())
          .getDisplayName());
        if (i.hasNext()) {
          relationNameBuilder.appendHtmlConstant(", ");
        }
      }
    } else {
      relationNameBuilder.appendEscaped(table.getName()).appendHtmlConstant(" (");
      for (Iterator<ViewerReference> i = fk.getReferences().iterator(); i.hasNext();) {
        ViewerReference viewerReference = i.next();
        relationNameBuilder.appendEscaped(table.getColumns().get(viewerReference.getSourceColumnIndex())
          .getDisplayName());
        if (i.hasNext()) {
          relationNameBuilder.appendHtmlConstant(", ");
        }
      }
    }
    relationNameBuilder.appendHtmlConstant(") <i class=\"fa fa-arrow-right small\"></i> ");

    // relation target
    if (currentTableIsReferencedTableInForeignKey) {
      relationNameBuilder.appendEscaped(table.getName()).appendHtmlConstant(" (");
      for (Iterator<ViewerReference> i = fk.getReferences().iterator(); i.hasNext();) {
        ViewerReference viewerReference = i.next();
        relationNameBuilder.appendEscaped(table.getColumns().get(viewerReference.getReferencedColumnIndex())
          .getDisplayName());
        if (i.hasNext()) {
          relationNameBuilder.appendHtmlConstant(", ");
        }
      }
    } else {
      relationNameBuilder.appendEscaped(otherTable.getName()).appendHtmlConstant(" (");
      for (Iterator<ViewerReference> i = fk.getReferences().iterator(); i.hasNext();) {
        ViewerReference viewerReference = i.next();
        relationNameBuilder.appendEscaped(otherTable.getColumns().get(viewerReference.getReferencedColumnIndex())
          .getDisplayName());
        if (i.hasNext()) {
          relationNameBuilder.appendHtmlConstant(", ");
        }
      }
    }
    relationNameBuilder.appendHtmlConstant(")");

    HTMLPanel relName = new HTMLPanel(relationNameBuilder.toSafeHtml());
    relName.addStyleName("h4");
    header.add(relName);

    SafeHtmlBuilder descriptionBuilder = new SafeHtmlBuilder();

    descriptionBuilder.appendHtmlConstant("<div class=\"label\">Related table</div>");
    descriptionBuilder
      .appendHtmlConstant("<div class=\"value\">")
      .appendHtmlConstant(
        new Hyperlink(otherTable.getSchemaName() + " . " + otherTable.getName(), HistoryManager.linkToTable(
          database.getUUID(), otherTable.getUUID())).toString()).appendHtmlConstant("</div>");

    descriptionBuilder.appendHtmlConstant("<div class=\"label\">Foreign key name</div>");
    descriptionBuilder.appendHtmlConstant("<div class=\"value\">").appendEscaped(fk.getName())
      .appendHtmlConstant("</div>");

    if (ViewerStringUtils.isNotBlank(fk.getDescription())) {
      descriptionBuilder.appendHtmlConstant("<div class=\"label\">Foreign key description</div>");
      descriptionBuilder.appendHtmlConstant("<div class=\"value\">").appendEscaped(fk.getDescription())
        .appendHtmlConstant("</div>");
    }

    header.add(new HTMLPanel(descriptionBuilder.toSafeHtml()));
    return header;
  }

  private TableSearchPanel createTableSearchPanelFromReference(Reference reference) {
    final ViewerTable otherTable = reference.table;

    // create filter to look for record values in other tables related to this
    // one by a foreign key
    boolean currentTableIsReferencedTableInForeignKey = reference.foreignKey.getReferencedTableUUID().equals(
      table.getUUID());
    List<FilterParameter> filterParameters = new ArrayList<>();
    for (ViewerReference viewerReference : reference.foreignKey.getReferences()) {
      String columnNameInCurrentTable;
      String columnNameInReferencedTable;

      if (currentTableIsReferencedTableInForeignKey) {
        columnNameInCurrentTable = table.getColumns().get(viewerReference.getReferencedColumnIndex()).getSolrName();
        columnNameInReferencedTable = otherTable.getColumns().get(viewerReference.getSourceColumnIndex()).getSolrName();
      } else {
        columnNameInCurrentTable = table.getColumns().get(viewerReference.getSourceColumnIndex()).getSolrName();
        columnNameInReferencedTable = otherTable.getColumns().get(viewerReference.getReferencedColumnIndex())
          .getSolrName();
      }

      ViewerCell viewerCell = record.getCells().get(columnNameInCurrentTable);
      if (viewerCell != null) {
        String value = viewerCell.getValue();
        filterParameters.add(new SimpleFilterParameter(columnNameInReferencedTable, value));
      } else {
        filterParameters.add(new EmptyKeyFilterParameter(columnNameInReferencedTable));
      }
    }
    Filter filter = new Filter(filterParameters);

    // create the table with the filter
    final TableSearchPanel tableSearchPanel = new TableSearchPanel();
    tableSearchPanel.provideSource(database, otherTable, filter);

    return tableSearchPanel;
  }

  private SafeHtml getCellHTML(ViewerColumn column) {
    String label = column.getDisplayName();

    String value = "NULL";
    ViewerCell cell = record.getCells().get(column.getSolrName());
    if (cell != null) {
      if (cell.getValue() != null) {
        value = cell.getValue();
      }
    }

    SafeHtmlBuilder b = new SafeHtmlBuilder();
    b.appendHtmlConstant("<div class=\"field\">");
    b.appendHtmlConstant("<div class=\"label\">");
    b.appendEscaped(label);
    b.appendHtmlConstant("</div>");
    b.appendHtmlConstant("<div class=\"value\">");
    b.appendEscaped(value);
    b.appendHtmlConstant("</div>");
    b.appendHtmlConstant("</div>");
    return b.toSafeHtml();
  }

  private static class Reference implements Comparable<Reference> {
    private ViewerTable table;
    private ViewerForeignKey foreignKey;

    Reference(ViewerTable table, ViewerForeignKey foreignKey) {
      this.table = table;
      this.foreignKey = foreignKey;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      Reference reference = (Reference) o;

      if (table == reference.table && foreignKey == reference.foreignKey) {
        return true;
      }

      return table.getSchemaName().equals(reference.table.getSchemaName())
        && foreignKey.getName().equals(reference.foreignKey.getName());
    }

    @Override
    public int hashCode() {
      return Objects.hash(table, foreignKey);
    }

    @Override
    public int compareTo(Reference o) {
      int nameComparison = this.table.getSchemaName().compareTo(o.table.getSchemaName());
      if (nameComparison == 0) {
        nameComparison = this.table.getName().compareTo(o.table.getName());
        if (nameComparison == 0) {
          return this.foreignKey.getName().compareTo(o.foreignKey.getName());
        } else {
          return nameComparison;
        }
      } else {
        return nameComparison;
      }
    }
  }
}
