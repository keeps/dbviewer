package com.databasepreservation.main.desktop.client.dbptk.wizard;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.ProgressData;
import com.databasepreservation.main.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.main.common.shared.client.common.Progressbar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

import java.util.HashMap;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ProgressBarPanel extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface ProgressBarPanelUiBinder extends UiBinder<Widget, ProgressBarPanel> {
  }

  private static ProgressBarPanelUiBinder uiBinder = GWT.create(ProgressBarPanelUiBinder.class);

  private Timer autoUpdateTimer = new Timer() {
    @Override
    public void run() {
      ProgressBarPanel.this.update();
    }
  };

  @UiField
  FlowPanel content;

  @UiField
  Progressbar progressBar;

  @UiField
  Label title, subTitle;

  private static HashMap<String, ProgressBarPanel> instances = new HashMap<>();
  private final String uuid;

  public static ProgressBarPanel getInstance(String uuid) {
    if (instances.get(uuid) == null) {
      instances.put(uuid, new ProgressBarPanel(uuid));
    }

    return instances.get(uuid);
  }

  private ProgressBarPanel(String uuid) {
    initWidget(uiBinder.createAndBindUi(this));
    this.uuid = uuid;
    title.setText(messages.wizardProgressTitle());
    subTitle.setText(messages.wizardProgressSubTitle());
    update();
  }

  private void init() {
    BrowserService.Util.getInstance().getProgressData(uuid, new DefaultAsyncCallback<ProgressData>() {
      @Override
      public void onSuccess(ProgressData result) {
        result.reset();
        progressBar.setCurrent(0);
        content.clear();
        stopUpdating();
        autoUpdateTimer.scheduleRepeating(1000);
      }
    });
  }

  @Override
  protected void onAttach() {
    init();
    super.onAttach();
  }

  public void clear(String uuid) {
    progressBar.setCurrent(0);
    content.clear();
    instances.put(uuid, null);
  }

  private void update() {
    BrowserService.Util.getInstance().getProgressData(uuid, new DefaultAsyncCallback<ProgressData>() {
      @Override
      public void onSuccess(ProgressData result) {
        update(result);
      }
    });
  }

  private void update(ProgressData progressData) {
    if (progressData.isDatabaseStructureRetrieved()) {
      int currentGlobalPercent = new Double(
          ((progressData.getProcessedRows() * 1.0D) / progressData.getTotalRows()) * 100).intValue();
      progressBar.setCurrent(currentGlobalPercent);

      final String totalTablesPercentage = buildPercentageMessage(messages.progressBarPanelTables(),
        progressData.getProcessedTables(), progressData.getTotalTables());
      final String totalRowsPercentage = buildPercentageMessage(messages.progressBarPanelRows(),
        progressData.getProcessedRows(), progressData.getTotalRows());
      final String currentTable = buildSimpleMessage(messages.progressBarPanelCurrentTables(),
        progressData.getCurrentTableName());
      final String currentTableRowsPercentage = buildPercentageMessage(messages.progressBarPanelCurrentRows(),
        progressData.getCurrentProcessedTableRows(), progressData.getCurrentTableTotalRows());

      addMessageToContent(1, totalTablesPercentage);
      addMessageToContent(2, totalRowsPercentage);
      addMessageToContent(3, currentTable);
      addMessageToContent(4, currentTableRowsPercentage);
    } else {
      final String retrieving = buildSimpleMessage("", messages.retrievingTableStructure());
      addMessageToContent(0, retrieving);
    }

    if (progressData.isFinished()) {
      stopUpdating();
    }
  }

  @Override
  protected void onDetach() {
    stopUpdating();
    super.onDetach();
  }

  private void stopUpdating() {
    if (autoUpdateTimer != null) {
      autoUpdateTimer.cancel();
    }
  }

  private void addMessageToContent(final int index, final String message) {
    Label newMessage = new Label(message);
    final int widgetCount = content.getWidgetCount();
    if (widgetCount > 0) {
      if (index < widgetCount) {
        Label lastMessage = (Label) content.getWidget(index);
        if (!newMessage.getText().equals(lastMessage.getText())) {
          content.insert(newMessage, index);
          content.remove(index + 1);
          content.getElement().setScrollTop(content.getElement().getScrollHeight());
        }
      } else {
        content.add(newMessage);
        content.getElement().setScrollTop(content.getElement().getScrollHeight());
      }
    } else {
      content.add(newMessage);
      content.getElement().setScrollTop(content.getElement().getScrollHeight());
    }
  }

  private String buildPercentageMessage(final String type, final long processed, final long total) {
    // Examples Table: X of Y (Z%), Rows: X of Y (Z%), Rows on current table: X of Y
    // (Z%)
    StringBuilder sb = new StringBuilder();

    sb.append(type).append(" ").append(processed).append(" ").append(messages.of()).append(" ").append(total);

    float percent = 0;
    if (total > 0) {
      percent = (processed * 1.0F) / total;
    }

    sb.append(" (").append(NumberFormat.getPercentFormat().format(percent)).append(")");
    sb.append("\n\n");

    return sb.toString();
  }

  private String buildSimpleMessage(final String type, final String message) {
    // Example: Current table: <name>
    return type + " " + message + "\n\n";
  }
}
