package com.databasepreservation.common.shared.client.common.utils;

import com.databasepreservation.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.common.shared.client.common.RightPanel;

/**
 * Main passes an instance of this class to DatabasePanel, so it can obtain a
 * RightPanel when convenient
 * 
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public abstract class RightPanelLoader {
  public abstract RightPanel load(ViewerDatabase database);
}