package com.databasepreservation.main.desktop.client.dbptk.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.ViewerConstants;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerMetadata;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSIARDBundle;
import com.databasepreservation.main.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.main.common.shared.client.common.LoadingDiv;
import com.databasepreservation.main.common.shared.client.common.dialogs.Dialogs;
import com.databasepreservation.main.common.shared.client.common.utils.ApplicationType;
import com.databasepreservation.main.common.shared.client.common.utils.JavascriptUtils;
import com.databasepreservation.main.common.shared.client.widgets.Toast;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MetadataControlPanel extends Composite {

  interface MetadataControlPanelUiBinder extends UiBinder<Widget, MetadataControlPanel> {
  }

  private static MetadataControlPanelUiBinder uiBinder = GWT.create(MetadataControlPanelUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, MetadataControlPanel> instances = new HashMap<>();
  private ViewerDatabase database = null;
  private ViewerSIARDBundle SIARDbundle = null;
  private Map<String, Boolean> mandatoryItems = new HashMap<>();

  @UiField
  Button buttonSave, buttonRevert;

  @UiField
  Label toolTip;

  @UiField
  LoadingDiv loading;

  public static MetadataControlPanel getInstance(String databaseUUID) {

    MetadataControlPanel instance = instances.get(databaseUUID);
    if (instance == null) {
      instance = new MetadataControlPanel();
      instances.put(databaseUUID, instance);
    }

    return instance;
  }

  private MetadataControlPanel() {
    initWidget(uiBinder.createAndBindUi(this));
    buttonSave.setEnabled(false);
  }

  public void init(ViewerDatabase database, ViewerSIARDBundle SIARDbundle) {
    this.database = database;
    this.SIARDbundle = SIARDbundle;
  }

  public void setMandatoryItems(String name, Boolean required) {
    mandatoryItems.put(name, required);
  }

  public void checkIfElementIsMandatory(String name, Widget element) {
    String value = null;
    if (element instanceof TextBoxBase) {
      value = ((TextBoxBase) element).getText();
    } else if (element instanceof DateBox) {
      value = ((DateBox) element).getDatePicker().getValue().toString();
      GWT.log("VALUE:::" + value);
    }
    if (mandatoryItems.get(name) != null) {
      if (value.isEmpty()) {
        mandatoryItems.replace(name, true);
      } else {
        mandatoryItems.replace(name, false);
      }
    }
    validate();
  }

  public void validate() {
    List<String> mandatoryItemsRequired = new ArrayList<>();
    buttonSave.setEnabled(true);
    for (Map.Entry<String, Boolean> entry : mandatoryItems.entrySet()) {
      if (entry.getValue()) {
        buttonSave.setEnabled(false);
        mandatoryItemsRequired.add(entry.getKey());
      }
    }

    buttonRevert.setVisible(true);
    toolTip.setVisible(true);
    if (mandatoryItemsRequired.isEmpty()) {
      toolTip.setText(messages.metadataHasUpdates());
    } else {
      toolTip.setText(messages.metadataMissingFields(mandatoryItemsRequired.toString()));
      toolTip.addStyleName("missing");
    }
  }

  private void updateMetadata() {
    ViewerMetadata metadata = database.getMetadata();

    loading.setVisible(true);

    BrowserService.Util.getInstance().updateMetadataInformation(metadata, SIARDbundle, database.getUUID(),
      database.getSIARDPath(), new DefaultAsyncCallback<ViewerMetadata>() {
        @Override
        public void onFailure(Throwable caught) {
          Toast.showError(messages.metadataFailureUpdated(), caught.getMessage());
          loading.setVisible(false);
        }

        @Override
        public void onSuccess(ViewerMetadata result) {
          loading.setVisible(false);
          Toast.showInfo(messages.metadataSuccessUpdated(), "");
          buttonRevert.setVisible(false);
          toolTip.setVisible(false);
          buttonSave.setEnabled(false);
        }
      });
  }

  @UiHandler("buttonSave")
  void buttonSaveHandler(ClickEvent e) {

    if (ApplicationType.getType().equals(ViewerConstants.ELECTRON)) {
      JavascriptUtils.confirmationDialog(messages.dialogUpdateMetadata(), messages.dialogConfirmUpdateMetadata(),
        messages.dialogCancel(), messages.dialogConfirm(), new DefaultAsyncCallback<Boolean>() {

          @Override
          public void onSuccess(Boolean confirm) {
            if (confirm) {
              updateMetadata();
            }
          }

        });
    } else {
      Dialogs.showConfirmDialog(messages.dialogConfirm(), messages.dialogConfirmUpdateMetadata(),
        messages.dialogCancel(), messages.dialogConfirm(), new DefaultAsyncCallback<Boolean>() {

          @Override
          public void onFailure(Throwable caught) {
            Toast.showError(messages.metadataFailureUpdated(), caught.getMessage());
          }

          @Override
          public void onSuccess(Boolean confirm) {
            if (confirm) {
              updateMetadata();
            }
          }
        });
    }
  }

  @UiHandler("buttonRevert")
  void cancelButtonHandler(ClickEvent e) {
    Window.Location.reload();
  }
}
