package com.owon.uppersoft.dso.view;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import com.owon.uppersoft.dso.global.DataHouse;

public class CDropTargetAdapter extends DropTargetAdapter {
	private DataHouse dh;

	public CDropTargetAdapter(DataHouse dh) {
		this.dh = dh;
	}

	@Override
	public void drop(DropTargetDropEvent dtde) {
		try {
			// Transferable tr = dtde.getTransferable();
			if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				List list = (List) (dtde.getTransferable()
						.getTransferData(DataFlavor.javaFileListFlavor));

				File f = (File) list.get(0);
				if (f != null) {
					dh.controlManager.binIn.openfile(dh, f);
				}
				dtde.dropComplete(true);
			} else {
				dtde.rejectDrop();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (UnsupportedFlavorException ufe) {
			ufe.printStackTrace();
		}
	}
}