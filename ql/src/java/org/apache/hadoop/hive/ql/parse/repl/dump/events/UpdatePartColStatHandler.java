/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hive.ql.parse.repl.dump.events;

import org.apache.hadoop.hive.metastore.api.NotificationEvent;
import org.apache.hadoop.hive.metastore.messaging.UpdatePartitionColumnStatMessage;
import org.apache.hadoop.hive.ql.io.AcidUtils;
import org.apache.hadoop.hive.ql.metadata.Table;
import org.apache.hadoop.hive.ql.parse.repl.DumpType;
import org.apache.hadoop.hive.ql.parse.repl.dump.Utils;
import org.apache.hadoop.hive.ql.parse.repl.load.DumpMetaData;

class UpdatePartColStatHandler extends AbstractEventHandler<UpdatePartitionColumnStatMessage> {

  UpdatePartColStatHandler(NotificationEvent event) {
    super(event);
  }

  @Override
  UpdatePartitionColumnStatMessage eventMessage(String stringRepresentation) {
    return deserializer.getUpdatePartitionColumnStatMessage(stringRepresentation);
  }

  @Override
  public void handle(Context withinContext) throws Exception {
    LOG.info("Processing#{} UpdatePartitionTableColumnStat message : {}", fromEventId(),
            eventMessageAsJSON);

    org.apache.hadoop.hive.metastore.api.Table tableObj = eventMessage.getTableObject();
    if (tableObj == null) {
      LOG.debug("Event#{} was an event of type {} with no table listed", fromEventId(),
              event.getEventType());
      return;
    }

    // Statistics without any data does not make sense.
    if (withinContext.replicationSpec.isMetadataOnly()) {
      return;
    }

    if (!Utils.shouldReplicate(withinContext.replicationSpec, new Table(tableObj), true,
                              withinContext.hiveConf)) {
      return;
    }

    DumpMetaData dmd = withinContext.createDmd(this);
    dmd.setPayload(eventMessageAsJSON);
    dmd.write();
  }

  @Override
  public DumpType dumpType() {
    return DumpType.EVENT_UPDATE_PART_COL_STAT;
  }
}
