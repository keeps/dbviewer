package com.databasepreservation.main.desktop.client.dbptk.metadata.schemas.views;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSIARDBundle;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerView;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.main.desktop.client.dbptk.metadata.MetadataControlPanel;
import com.databasepreservation.main.desktop.client.dbptk.metadata.MetadataPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MetadataViewPanel extends MetadataPanel {

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {

  }

  interface MetadataViewUiBinder extends UiBinder<Widget, MetadataViewPanel> {
  }

  private static MetadataViewUiBinder uiBinder = GWT.create(MetadataViewUiBinder.class);

  @UiField
  SimplePanel mainHeader;

  @UiField
  TextArea description;

  @UiField
  TabPanel tabPanel;

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, MetadataViewPanel> instances = new HashMap<>();
  private ViewerSIARDBundle SIARDbundle;
  private MetadataControlPanel controls;
  private ViewerDatabase database;
  private ViewerSchema schema;
  private ViewerView view;

  public static MetadataViewPanel getInstance(ViewerDatabase database, ViewerSIARDBundle SIARDbundle, String schemaUUID,
                                              String viewUUID) {
    String separator = "/";
    String code = database.getUUID() + separator + viewUUID;

    MetadataViewPanel instance = instances.get(code);
    if (instance == null) {
      instance = new MetadataViewPanel(database, SIARDbundle, schemaUUID, viewUUID);
      instances.put(code, instance);
    }

    return instance;
  }

  private MetadataViewPanel(ViewerDatabase database, ViewerSIARDBundle SIARDbundle, String schemaUUID, String viewUUID) {
    this.database = database;
    this.SIARDbundle = SIARDbundle;
    view = database.getMetadata().getView(viewUUID);
    schema = database.getMetadata().getSchema(schemaUUID);
    controls = MetadataControlPanel.getInstance(database.getUUID());

    GWT.log("MetadataViewPanel::" + view.getName());

    initWidget(uiBinder.createAndBindUi(this));
    init();
  }

  private void init() {
    Label viewName = new Label();
    viewName.setText(schema.getName()+"."+view.getName());
    mainHeader.setWidget(viewName);

    description.getElement().setAttribute("placeholder", messages.viewDoesNotContainDescription());
    description.setText(view.getDescription());
    description.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        view.setDescription(description.getText());
        SIARDbundle.setView(schema.getName(), view.getName(), description.getText());
        controls.validate();
      }
    });
    description.addFocusHandler(new FocusHandler() {
      @Override
      public void onFocus(FocusEvent event) {
        description.selectAll();
      }
    });

    tabPanel.add(new MetadataViewColumns(SIARDbundle,schema, view, controls).createTable(), messages.columns());
    tabPanel.add(new MetadataViewQuery(view).createInfo(), messages.query());

    tabPanel.selectTab(0);

  }
}