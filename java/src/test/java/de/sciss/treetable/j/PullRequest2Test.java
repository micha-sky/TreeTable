package de.sciss.treetable.j;

import javax.swing.*;
import java.awt.*;

public class PullRequest2Test
{
	public static void main (String[] args)
	{
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run ()
			{
				JFrame frame = new JFrame("Frame");
				TreeTable table = new TreeTable(buildRoot());
				table.setAutoCreateRowSorter(true); // bug appears when the row sorter is activated
				table.setShowsRootHandles(true);
				table.setRootVisible(false);
				frame.getContentPane().add(new JScrollPane(table));
				frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
				frame.setPreferredSize(new Dimension(1000,300));
				frame.pack();

				frame.setVisible(true);
			}
		});
	}

	private static TreeTableNode buildRoot ()
	{
		DefaultTreeTableNode root = new DefaultTreeTableNode(new Object(), false);
		DefaultTreeTableNode node1 = new DefaultTreeTableNode("Step 1. Open This Node", false);
		DefaultTreeTableNode subNode1 = new DefaultTreeTableNode("Ignore this node", false);
		node1.add(subNode1);
		subNode1.add(new DefaultTreeTableNode("SubNode1-1", false));
		subNode1 = new DefaultTreeTableNode("Step 2. Open This node and then Step 3. Click checkbox ->", false);
		node1.add(subNode1);
		subNode1.add(new DefaultTreeTableNode("SubNode2-1", false));
		root.add(node1);
		return root;
	}
}
