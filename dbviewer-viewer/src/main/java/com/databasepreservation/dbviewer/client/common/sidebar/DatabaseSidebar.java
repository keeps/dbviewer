package com.databasepreservation.dbviewer.client.common.sidebar;

import java.util.HashMap;
import java.util.Map;

import org.roda.core.data.v2.index.IsIndexed;

import com.databasepreservation.dbviewer.client.BrowserService;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerMetadata;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerSchema;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerTable;
import com.databasepreservation.dbviewer.shared.client.Tools.FontAwesomeIconManager;
import com.databasepreservation.dbviewer.shared.client.Tools.HistoryManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DatabaseSidebar extends Composite {
  private static Map<String, DatabaseSidebar> instances = new HashMap<>();

  /**
   * Creates a new DatabaseSidebar, rarely hitting the database more than once
   * for each database.
   * 
   * @param databaseUUID
   *          the database UUID
   * @return a new DatabaseSidebar
   */
  public static DatabaseSidebar getInstance(String databaseUUID) {
    String code = databaseUUID;

    DatabaseSidebar instance = instances.get(code);
    if (instance == null || instance.database == null) {
      instance = new DatabaseSidebar(databaseUUID);
      instances.put(code, instance);
    } else {
      // workaround because the same DatabaseSidebar can not belong to multiple
      // widgets
      return new DatabaseSidebar(instance);
    }
    return instance;
  }

  interface DatabaseSidebarUiBinder extends UiBinder<Widget, DatabaseSidebar> {
  }

  private static DatabaseSidebarUiBinder uiBinder = GWT.create(DatabaseSidebarUiBinder.class);

  @UiField
  FlowPanel sidebarGroup;

  private ViewerDatabase database;

  /**
   * Clone constructor, because the same DatabaseSidebar can not be child in
   * more than one widget
   * 
   * @param other
   *          the DatabaseSidebar used in another widget
   */
  private DatabaseSidebar(DatabaseSidebar other) {
    initWidget(uiBinder.createAndBindUi(this));
    database = other.database;
    init();
  }

  /**
   * Use DatabaseSidebar.getInstance to obtain an instance
   */
  private DatabaseSidebar(String databaseUUID) {
    initWidget(uiBinder.createAndBindUi(this));

    BrowserService.Util.getInstance().retrieve(ViewerDatabase.class.getName(), databaseUUID,
      new AsyncCallback<IsIndexed>() {
        @Override
        public void onFailure(final Throwable caught) {
          throw new RuntimeException(caught);
        }

        @Override
        public void onSuccess(IsIndexed result) {
          database = (ViewerDatabase) result;
          init();
        }
      });
  }

  private void init() {
    // database metadata
    final ViewerMetadata metadata = database.getMetadata();

    sidebarGroup.add(new SidebarItem("Database").addIcon(FontAwesomeIconManager.DATABASE).setH5()
      .setIndent0());

    sidebarGroup.add(new SidebarHyperlink("Information", HistoryManager.linkToDatabase(database.getUUID()))
      .addIcon(FontAwesomeIconManager.DATABASE_INFORMATION).setH6().setIndent1());

    sidebarGroup.add(new SidebarHyperlink("Users & Roles", HistoryManager.linkToDatabase(database.getUUID()))
      .addIcon(FontAwesomeIconManager.DATABASE_USERS).setH6().setIndent1());

    for (final ViewerSchema schema : metadata.getSchemas()) {
      sidebarGroup.add(new SidebarItem("Schema " + schema.getName()).addIcon(FontAwesomeIconManager.SCHEMA).setH5().setIndent0());

      sidebarGroup.add(new SidebarHyperlink("Structure", HistoryManager.linkToSchemaStructure(database.getUUID(),
        schema.getUUID())).addIcon(FontAwesomeIconManager.SCHEMA_STRUCTURE).setH6().setIndent1());

      sidebarGroup.add(new SidebarHyperlink("Routines", HistoryManager.linkToSchemaRoutines(database.getUUID(),
        schema.getUUID())).addIcon(FontAwesomeIconManager.SCHEMA_ROUTINES).setH6().setIndent1());

      sidebarGroup.add(new SidebarHyperlink("Triggers", HistoryManager.linkToSchemaTriggers(database.getUUID(),
        schema.getUUID())).addIcon(FontAwesomeIconManager.SCHEMA_TRIGGERS).setH6().setIndent1());

      sidebarGroup.add(new SidebarHyperlink("Views", HistoryManager.linkToSchemaViews(database.getUUID(), schema.getUUID()))
        .addIcon(FontAwesomeIconManager.SCHEMA_VIEWS).setH6().setIndent1());

      sidebarGroup.add(new SidebarItem("Data").addIcon(FontAwesomeIconManager.SCHEMA_DATA).setH6().setIndent1());

      for (ViewerTable table : schema.getTables()) {
        sidebarGroup.add(new SidebarHyperlink(table.getName(), HistoryManager.linkToTable(database.getUUID(),
          table.getUUID())).addIcon(FontAwesomeIconManager.TABLE).setH6().setIndent2());
      }
    }
  }
}