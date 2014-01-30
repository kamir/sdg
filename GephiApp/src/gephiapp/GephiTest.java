/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 Gephi is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.

 Gephi is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package gephiapp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.type.Interval;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.project.api.ProjectController;
import org.openide.util.Lookup;

public class GephiTest {

    public void script() {
        //Initialize graph
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();

        //Get some models and utility controllers
        DynamicModel dynamicModel = Lookup.getDefault().lookup(DynamicController.class).getModel();//Necessary to create dynamic model, otherwise dynamics usage won't work
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
        AttributeColumnsController acc = Lookup.getDefault().lookup(AttributeColumnsController.class);
        Graph graph = graphModel.getGraph();
        
        //Add sample graph structure data
        Node n1;
        Node n2;
        n1 = graphModel.factory().newNode();
        n2 = graphModel.factory().newNode();
        Edge edge = graphModel.factory().newEdge(n1, n2);

        graph.addNode(n1);
        graph.addNode(n2);
        graph.addEdge(edge);

        n1.getNodeData().setLabel("Node 1");
        n2.getNodeData().setLabel("Node 2");
        edge.getEdgeData().setLabel("Edge 1");

        System.out.println(graph.getNodeCount() + " nodes");
        System.out.println(graph.getEdgeCount() + " edges");
        System.out.println();

        //Add dynamic properties
        AttributeColumn nodesTimeIntervalColumn = acc.addAttributeColumn(attributeModel.getNodeTable(), "Time Interval", AttributeType.TIME_INTERVAL);//Existence intervals column for nodes
        AttributeColumn edgesTimeIntervalColumn = acc.addAttributeColumn(attributeModel.getEdgeTable(), "Time Interval", AttributeType.TIME_INTERVAL);//Existence intervals column for edges
        
        //Make edge weight dynamic:
        acc.convertAttributeColumnToDynamic(attributeModel.getEdgeTable(), attributeModel.getEdgeTable().getColumn("Weight"), 2000, Double.POSITIVE_INFINITY, false, false);
        
        System.out.println("All node columns:");
        for (AttributeColumn col : attributeModel.getNodeTable().getColumns()) {
            System.out.println(col.getTitle() + " - " + col.getType());
        }
        System.out.println();
        System.out.println("All edge columns:");
        for (AttributeColumn col : attributeModel.getEdgeTable().getColumns()) {
            System.out.println(col.getTitle() + " - " + col.getType());
        }
        System.out.println();

        //Time interval for node 1
        List<Interval> interval1List = new ArrayList<Interval>();
        interval1List.add(new Interval(2000, 2005));
        interval1List.add(new Interval(2007, 2010));
        interval1List.add(new Interval(2012, 2013));
        TimeInterval interval1 = new TimeInterval(interval1List);
        n1.getNodeData().getAttributes().setValue(nodesTimeIntervalColumn.getIndex(), interval1);
        System.out.println(n1.getNodeData().getAttributes().getValue(nodesTimeIntervalColumn.getIndex()));
        
        //Time Interval for node 2 (alternative, simpler way with auto-parsing)
        n2.getNodeData().getAttributes().setValue(nodesTimeIntervalColumn.getIndex(), "[2001, 2003]; [2005,2007); (2008, 2009)");
        //n2.getNodeData().getAttributes().setValue("Time Interval", ...); also valid
        System.out.println(n2.getNodeData().getAttributes().getValue("Time Interval"));

        edge.getEdgeData().getAttributes().setValue("Time Interval", "[2000, Infinity]");
        System.out.println(edge.getEdgeData().getAttributes().getValue("time interval"));
        edge.getEdgeData().getAttributes().setValue("Weight", "[2000, 2001, 1.0]; [2001, 2006, 2.0]; [2006, Infinity, 1.5]");//A dynamic attribute such as weight (Dynamic Float) takes interval bounds and value for the interval
        System.out.println(edge.getEdgeData().getAttributes().getValue("weight"));
        
        try {
            Thread.sleep(1000);//IMPORTANT. Asynchronous events are sent to dynamic model when we add dynamic columns and dynamic values. Allow some time for dynamic model to process these events in order to have a correct GEXF file.
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        
        System.out.println("Is dynamic graph? " + dynamicModel.isDynamicGraph());
        System.out.println("Min dynamic timestamp " + dynamicModel.getMin());
        System.out.println("Max dynamic timestamp " + dynamicModel.getMax());
        
        //Export GEXF
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        try {
            ec.exportFile(new File("sample.gexf"));
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
    }

    public static void main(String[] args) {
        new GephiTest().script();
    }
}
