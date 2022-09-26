/*
 * Copyright (c) 1998, 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.sun.hotspot.igv.view;

import com.sun.hotspot.igv.data.Properties;
import com.sun.hotspot.igv.data.*;
import com.sun.hotspot.igv.data.services.Scheduler;
import com.sun.hotspot.igv.difference.Difference;
import com.sun.hotspot.igv.filter.ColorFilter;
import com.sun.hotspot.igv.filter.FilterChain;
import com.sun.hotspot.igv.graph.Diagram;
import com.sun.hotspot.igv.graph.Figure;
import com.sun.hotspot.igv.graph.MatcherSelector;
import com.sun.hotspot.igv.settings.Settings;
import com.sun.hotspot.igv.util.RangeSliderModel;
import java.awt.Color;
import java.util.*;
import org.openide.util.Lookup;

/**
 *
 * @author Thomas Wuerthinger
 */
public class DiagramViewModel extends RangeSliderModel implements ChangedListener<RangeSliderModel> {

    // Warning: Update setData method if fields are added
    private Group group;
    private ArrayList<InputGraph> graphs;
    private Set<Integer> hiddenNodes;
    private Set<Integer> selectedNodes;
    private FilterChain filterChain;
    private FilterChain sequenceFilterChain;
    private Diagram diagram;
    private InputGraph cachedInputGraph;
    private final ChangedEvent<DiagramViewModel> diagramChangedEvent;
    private final ChangedEvent<DiagramViewModel> viewChangedEvent;
    private final ChangedEvent<DiagramViewModel> hiddenNodesChangedEvent;
    private final ChangedEvent<DiagramViewModel> viewPropertiesChangedEvent;
    private boolean showSea;
    private boolean showBlocks;
    private boolean showCFG;
    private boolean showNodeHull;
    private boolean showEmptyBlocks;
    private final ChangedListener<FilterChain> filterChainChangedListener = source -> updateDiagram();

    @Override
    public DiagramViewModel copy() {
        DiagramViewModel result = new DiagramViewModel(cachedInputGraph, filterChain, sequenceFilterChain);
        result.setData(this);
        return result;
    }

    public Group getGroup() {
        return group;
    }

    public void setData(DiagramViewModel newModel) {
        super.setData(newModel);

        if (group != newModel.group) {
            if (group != null) {
                group.getChangedEvent().removeListener(groupContentChangedListener);
            }
            group = newModel.group;
            if (group != null) {
                group.getChangedEvent().addListener(groupContentChangedListener);
            }
            filterGraphs();
        }

        boolean diagramChanged = (filterChain != newModel.filterChain);
        this.filterChain = newModel.filterChain;
        diagramChanged |= (sequenceFilterChain != newModel.sequenceFilterChain);
        this.sequenceFilterChain = newModel.sequenceFilterChain;
        diagramChanged |= (diagram != newModel.diagram);
        this.diagram = newModel.diagram;
        boolean viewChanged = (hiddenNodes != newModel.hiddenNodes);
        this.hiddenNodes = newModel.hiddenNodes;
        viewChanged |= (selectedNodes != newModel.selectedNodes);
        this.selectedNodes = newModel.selectedNodes;
        boolean viewPropertiesChanged = (showSea != newModel.showSea);
        this.showSea = newModel.showSea;
        viewPropertiesChanged |= (showBlocks != newModel.showBlocks);
        this.showBlocks = newModel.showBlocks;
        viewPropertiesChanged |= (showCFG != newModel.showCFG);
        this.showCFG = newModel.showCFG;
        viewPropertiesChanged |= (showNodeHull != newModel.showNodeHull);
        this.showNodeHull = newModel.showNodeHull;

        if (diagramChanged) {
            diagramChangedEvent.fire();
        }
        if (viewPropertiesChanged) {
            viewPropertiesChangedEvent.fire();
        }
        if (viewChanged) {
            viewChangedEvent.fire();
        }
    }

    public boolean getShowSea() {
        return showSea;
    }

    public void setShowSea(boolean b) {
        showSea = b;
        viewPropertiesChangedEvent.fire();
    }

    public boolean getShowBlocks() {
        return showBlocks;
    }

    public void setShowBlocks(boolean b) {
        showBlocks = b;
        viewPropertiesChangedEvent.fire();
    }

    public boolean getShowCFG() {
        return showCFG;
    }

    public void setShowCFG(boolean b) {
        showCFG = b;
        viewPropertiesChangedEvent.fire();
    }

    public boolean getShowNodeHull() {
        return showNodeHull;
    }

    public void setShowNodeHull(boolean b) {
        showNodeHull = b;
        viewPropertiesChangedEvent.fire();
    }

    public boolean getShowEmptyBlocks() {
        return showEmptyBlocks;
    }

    public void setShowEmptyBlocks(boolean b) {
        showEmptyBlocks = b;
        viewPropertiesChangedEvent.fire();
    }

    public DiagramViewModel(InputGraph graph, FilterChain filterChain, FilterChain sequenceFilterChain) {
        super(Collections.singletonList("default"));

        this.showSea = Settings.get().getInt(Settings.DEFAULT_VIEW, Settings.DEFAULT_VIEW_DEFAULT) == Settings.DefaultView.SEA_OF_NODES;
        this.showBlocks = Settings.get().getInt(Settings.DEFAULT_VIEW, Settings.DEFAULT_VIEW_DEFAULT) == Settings.DefaultView.CLUSTERED_SEA_OF_NODES;
        this.showCFG = Settings.get().getInt(Settings.DEFAULT_VIEW, Settings.DEFAULT_VIEW_DEFAULT) == Settings.DefaultView.CONTROL_FLOW_GRAPH;
        this.showNodeHull = true;
        this.showEmptyBlocks = true;
        this.group = graph.getGroup();
        filterGraphs();
        assert filterChain != null;
        this.filterChain = filterChain;
        assert sequenceFilterChain != null;
        this.sequenceFilterChain = sequenceFilterChain;
        hiddenNodes = new HashSet<>();
        selectedNodes = new HashSet<>();
        super.getChangedEvent().addListener(this);
        diagramChangedEvent = new ChangedEvent<>(this);
        viewChangedEvent = new ChangedEvent<>(this);
        hiddenNodesChangedEvent = new ChangedEvent<>(this);
        viewPropertiesChangedEvent = new ChangedEvent<>(this);

        groupChangedEvent = new ChangedEvent<>(this);
        ChangedListener<DiagramViewModel> groupChangedListener = new ChangedListener<DiagramViewModel>() {

            private Group oldGroup;

            @Override
            public void changed(DiagramViewModel source) {
                if (oldGroup != null) {
                    oldGroup.getChangedEvent().removeListener(groupContentChangedListener);
                }
                group.getChangedEvent().addListener(groupContentChangedListener);
                oldGroup = group;
            }
        };
        groupChangedEvent.addListener(groupChangedListener);
        groupChangedEvent.fire();

        filterChain.getChangedEvent().addListener(filterChainChangedListener);
        sequenceFilterChain.getChangedEvent().addListener(filterChainChangedListener);

        selectGraph(graph);
    }

    private final ChangedListener<Group> groupContentChangedListener = new ChangedListener<Group>() {

        @Override
        public void changed(Group source) {
            assert source == group;
            if (group.getGraphs().isEmpty()) {
                // If the group has been emptied, all corresponding graph views
                // will be closed, so do nothing.
                return;
            }
            filterGraphs();
            setSelectedNodes(selectedNodes);
        }
    };

    public ChangedEvent<DiagramViewModel> getDiagramChangedEvent() {
        return diagramChangedEvent;
    }

    public ChangedEvent<DiagramViewModel> getViewChangedEvent() {
        return viewChangedEvent;
    }

    public ChangedEvent<DiagramViewModel> getHiddenNodesChangedEvent() {
        return hiddenNodesChangedEvent;
    }

    public ChangedEvent<DiagramViewModel> getViewPropertiesChangedEvent() {
        return viewPropertiesChangedEvent;
    }

    public Set<Integer> getSelectedNodes() {
        return selectedNodes;
    }

    public Set<Integer> getHiddenNodes() {
        return hiddenNodes;
    }

    public void setSelectedNodes(Set<Integer> nodes) {
        this.selectedNodes = nodes;
        List<Color> colors = new ArrayList<>();
        for (String ignored : getPositions()) {
            colors.add(Color.black);
        }
        if (nodes.size() >= 1) {
            for (Integer id : nodes) {
                if (id < 0) {
                    id = -id;
                }
                InputNode last = null;
                int index = 0;
                for (InputGraph g : graphs) {
                    Color curColor = colors.get(index);
                    InputNode cur = g.getNode(id);
                    if (cur != null) {
                        if (last == null) {
                            curColor = Color.green;
                        } else {
                            if (last.equals(cur) && last.getProperties().equals(cur.getProperties())) {
                                if (curColor == Color.black) {
                                    curColor = Color.white;
                                }
                            } else {
                                if (curColor != Color.green) {
                                    curColor = Color.orange;
                                }
                            }
                        }
                    }
                    last = cur;
                    colors.set(index, curColor);
                    index++;
                }
            }
        }
        setColors(colors);
        viewChangedEvent.fire();
    }

    public void showFigures(Collection<Figure> f) {
        HashSet<Integer> newHiddenNodes = new HashSet<>(getHiddenNodes());
        for (Figure fig : f) {
            newHiddenNodes.remove(fig.getInputNode().getId());
        }
        setHiddenNodes(newHiddenNodes);
    }


    public Set<Figure> getSelectedFigures() {
        Set<Figure> result = new HashSet<>();
        for (Figure f : diagram.getFigures()) {
            if (getSelectedNodes().contains(f.getInputNode().getId())) {
                result.add(f);
            }
        }
        return result;
    }

    public void showAll(final Collection<Figure> f) {
        showFigures(f);
    }

    public void showOnly(final Set<Integer> nodes) {
        final HashSet<Integer> allNodes = new HashSet<>(getGroup().getAllNodes());
        allNodes.removeAll(nodes);
        setHiddenNodes(allNodes);
    }

    public void setHiddenNodes(Set<Integer> nodes) {
        this.hiddenNodes = nodes;
        hiddenNodesChangedEvent.fire();
    }

    public FilterChain getSequenceFilterChain() {
        return filterChain;
    }

    private void updateDiagram() {
        // clear diagram
        InputGraph graph = getGraph();
        if (graph.getBlocks().isEmpty()) {
            Scheduler s = Lookup.getDefault().lookup(Scheduler.class);
            graph.clearBlocks();
            s.schedule(graph);
            graph.ensureNodesInBlocks();
        }
        diagram = new Diagram(graph,
                Settings.get().get(Settings.NODE_TEXT, Settings.NODE_TEXT_DEFAULT),
                Settings.get().get(Settings.NODE_SHORT_TEXT, Settings.NODE_SHORT_TEXT_DEFAULT),
                Settings.get().get(Settings.NODE_TINY_TEXT, Settings.NODE_TINY_TEXT_DEFAULT));
        getFilterChain().apply(diagram, getSequenceFilterChain());
        if (graph.isDiffGraph()) {
            ColorFilter f = new ColorFilter("");
            f.addRule(stateColorRule("same",    Color.white));
            f.addRule(stateColorRule("changed", Color.orange));
            f.addRule(stateColorRule("new",     Color.green));
            f.addRule(stateColorRule("deleted", Color.red));
            f.apply(diagram);
        }

        diagramChangedEvent.fire();
    }

    public FilterChain getFilterChain() {
        return filterChain;
    }

    /*
     * Select the set of graphs to be presented.
     */
    private void filterGraphs() {
        ArrayList<InputGraph> result = new ArrayList<>();
        List<String> positions = new ArrayList<>();
        for (InputGraph graph : group.getGraphs()) {
            result.add(graph);
            positions.add(graph.getName());
        }
        this.graphs = result;
        setPositions(positions);
    }

    public InputGraph getFirstGraph() {
        if (getFirstPosition() < graphs.size()) {
            return graphs.get(getFirstPosition());
        }
        return graphs.get(graphs.size() - 1);
    }

    public InputGraph getSecondGraph() {
        if (getSecondPosition() < graphs.size()) {
            return graphs.get(getSecondPosition());
        }
        return getFirstGraph();
    }

    public void selectGraph(InputGraph g) {
        int index = graphs.indexOf(g);
        assert index != -1;
        setPositions(index, index);
    }

    private static ColorFilter.ColorRule stateColorRule(String state, Color color) {
        return new ColorFilter.ColorRule(new MatcherSelector(new Properties.RegexpPropertyMatcher("state", state)), color);
    }

    public Diagram getDiagram() {
        diagram.setCFG(getShowCFG());
        return diagram;
    }

    public InputGraph getGraph() {
        return cachedInputGraph;
    }

    @Override
    public void changed(RangeSliderModel source) {
        if (getFirstGraph() != getSecondGraph()) {
            cachedInputGraph = Difference.createDiffGraph(getFirstGraph(), getSecondGraph());
        } else {
            cachedInputGraph = getFirstGraph();
        }
        updateDiagram();
    }

    void close() {
        filterChain.getChangedEvent().removeListener(filterChainChangedListener);
        sequenceFilterChain.getChangedEvent().removeListener(filterChainChangedListener);
        getChangedEvent().fire();
    }

    Iterable<InputGraph> getGraphsForward() {
        return () -> new Iterator<InputGraph>() {
            int index = getFirstPosition();

            @Override
            public boolean hasNext() {
                return index + 1 < graphs.size();
            }

            @Override
            public InputGraph next() {
                return graphs.get(++index);
            }
        };
    }

    Iterable<InputGraph> getGraphsBackward() {
        return () -> new Iterator<InputGraph>() {
            int index = getFirstPosition();

            @Override
            public boolean hasNext() {
                return index - 1 > 0;
            }

            @Override
            public InputGraph next() {
                return graphs.get(--index);
            }
        };
    }
}
