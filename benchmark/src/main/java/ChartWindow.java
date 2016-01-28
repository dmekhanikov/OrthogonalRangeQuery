import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;

public class ChartWindow extends JFrame {

    public ChartWindow(DefaultCategoryDataset dataset, String windowTitle, String chartTitle) {
        super(windowTitle);
        JFreeChart lineChart = ChartFactory.createLineChart(
                chartTitle,
                "Number of points",
                "Time, milliseconds",
                dataset,
                PlotOrientation.VERTICAL,
                true,true,false);

        ChartPanel chartPanel = new ChartPanel(lineChart);
        chartPanel.setPreferredSize( new java.awt.Dimension(800, 600));
        setContentPane(chartPanel);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
