/*
 * Autopsy Forensic Browser
 * 
 * Copyright 2011 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sleuthkit.autopsy.datamodel;

import java.util.Arrays;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.sleuthkit.datamodel.SleuthkitCase;

/**
 *
 * @author dfickling
 */
public class RecentFilesChildren extends ChildFactory<RecentFiles.RecentFilesFilter>{
    
    SleuthkitCase skCase;
    
    public RecentFilesChildren(SleuthkitCase skCase) {
        this.skCase = skCase;
    }

    @Override
    protected boolean createKeys(List<RecentFiles.RecentFilesFilter> list) {
        list.addAll(Arrays.asList(RecentFiles.RecentFilesFilter.values()));
        return true;
    }
    
    @Override
    protected Node createNodeForKey(RecentFiles.RecentFilesFilter key){
        return new RecentFilesFilterNode(skCase, key);
    }
    
}
