package com.owon.uppersoft.vds.print;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

/*
 * Actual printer printing program.
 */
public class Printer implements Printable {

	// A4 paper has an actual width and length of 21 cm and 29.7 cm.
	// 1 inch = 2.54 cm.
	public static final int A4PageWidth = 595;// 595=21/2.54*72
	public static final int A4PageHeight = 842;// 842=29.7/2.54*72

	// the default page margin is 20 mm, the default dpi for printing in the Java Print API is 7272  57=20/25.4*72
	public int A4EdgeLeftSpace = 57;
	public int A4EdgeRightSpace = 57;
	public int A4EdgeUpSpace = 57;
	public int A4EdgeDownSpace = 57;

	PrinterPreviewFrame ppf;

	public Printer(PrinterPreviewFrame ppf) {
		this.ppf=ppf;

		Rectangle r = ppf.getPageEdgeSpace();
		A4EdgeLeftSpace = (int) (r.x / 25.4 * 72);
		A4EdgeRightSpace = (int) (r.y / 25.4 * 72);
		A4EdgeUpSpace = (int) (r.width / 25.4 * 72);
		A4EdgeDownSpace = (int) (r.height / 25.4 * 72);

		PrinterJob printJob = PrinterJob.getPrinterJob();
		Book book = new Book();
		PageFormat pf = new PageFormat();
		Paper p = new Paper();

		if (ppf.ppc.isVertical) {
			pf.setOrientation(PageFormat.PORTRAIT); // vertical
		} else {
			pf.setOrientation(PageFormat.LANDSCAPE);// horizontal
		}

		// set to the actual paper size
		p.setSize(A4PageWidth, A4PageHeight);
		// the actual imageable area of the paper, i.e. the actual paper length and width minus the margins.
		p.setImageableArea(A4EdgeLeftSpace, A4EdgeUpSpace, A4PageWidth
				- A4EdgeLeftSpace - A4EdgeRightSpace, A4PageHeight
				- A4EdgeUpSpace - A4EdgeDownSpace);

		


		pf.setPaper(p);
		book.append(this, pf);

		printJob.setPageable(book);
		if (printJob.printDialog()) {
			try {
				printJob.print();
			} catch (PrinterException pe) {
				pe.printStackTrace();
			}
		}

	}

	// set red dashed line
	float[] dash2 = { 6.0f, 4.0f, 2.0f, 4.0f, 2.0f, 4.0f };
	BasicStroke bs = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_MITER, 10.0f, dash2, 0.0f);

	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
			throws PrinterException {
		Graphics2D g2d = (Graphics2D) graphics;

		g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

		g2d.setStroke(bs);
		g2d.setPaint(Color.red);

		// draw a red dashed rectangle
		g2d.drawRect(0, 0, (int) pageFormat.getImageableWidth() - 2,
				(int) pageFormat.getImageableHeight() - 2);
		//draw the waveform background
		ppf.drawChartViewBG(g2d, (int)pageFormat.getImageableWidth() - 2);
		// draw the waveform
		g2d.setStroke(new BasicStroke());
		ppf.printChartView(g2d, (int) pageFormat.getImageableWidth() - 2,
				(int) pageFormat.getImageableHeight() - 2);//Graphics2D
		

//		Font titleFont = new Font("helvetica", Font.BOLD, 36);
//		g2d.setFont(titleFont);
//		g2d.drawLine(0, 30, 495, 30);
//		g2d.drawLine(0, 60, 465, 60);
//		g2d.drawLine(0, 90, 435, 90);
//		g2d.drawString("adfadfadf", 0, 20);

		return 0;
	}

}
