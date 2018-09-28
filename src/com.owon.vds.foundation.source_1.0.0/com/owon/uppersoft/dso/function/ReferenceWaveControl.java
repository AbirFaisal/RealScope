package com.owon.uppersoft.dso.function;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import com.owon.uppersoft.dso.function.ref.IReferenceWaveForm;
import com.owon.uppersoft.dso.function.ref.ReferenceFile;
import com.owon.uppersoft.dso.global.ControlManager;
import com.owon.uppersoft.vds.core.aspect.control.ISupportChannelsNumber;
import com.owon.uppersoft.vds.core.aspect.control.TimeConfProvider;
import com.owon.uppersoft.vds.core.aspect.control.VoltageProvider;
import com.owon.uppersoft.vds.core.control.MathControl;
import com.owon.uppersoft.vds.core.paint.ScreenContext;
import com.owon.uppersoft.vds.util.Pref;

/**
 * ReferenceWaveControl，是不同于WaveForm实现的另一种屏幕波形效果
 * 
 */
public class ReferenceWaveControl {

	private final int saveLimit = 8, showLimit = 4;
	private LinkedList<IReferenceWaveForm> referwfRAM = new LinkedList<IReferenceWaveForm>();
	private ReferenceFile[] referfileROM;// = new ReferenceFile[saveLimit];
	private int sourceId;

	private LinkedList<Integer> useableList;
	private List<String> rfObjNames;
	private ISupportChannelsNumber cm;

	public ReferenceFile[] getReferfileROM() {
		return referfileROM;
	}

	public int getSourceIdx() {
		return sourceId;
	}

	public void setSourceIdx(int sourceIdx) {
		this.sourceId = sourceIdx;
	}

	public ReferenceWaveControl(Pref p, ISupportChannelsNumber cm) {
		this.cm = cm;
		load(p);
		createReferFilesArray();
		initReferFilesArray();
	}

	private void createReferFilesArray() {
		referfileROM = new ReferenceFile[saveLimit];
		for (int i = 0; i < getReferfileROM().length; i++) {
			getReferfileROM()[i] = new ReferenceFile(i);
			getReferfileROM()[i].use = false;
		}
	}

	private void initReferFilesArray() {
		for (Integer i : useableList)
			getReferfileROM()[i].use = true;

		int i = 0;
		for (String n : rfObjNames) {
			if (n.equalsIgnoreCase("0"))
				getReferfileROM()[i].setName("");
			else
				getReferfileROM()[i].setName(n);
			i++;
		}
	}

	public void addtoRAMList(IReferenceWaveForm refwf, int dest) {
		getReferfileROM()[dest].use = true;

		removefromRAMList(dest);
		refwf.setObjIndex(dest);
		referwfRAM.add(refwf);
		// 限制显示个数
		if (referwfRAM.size() > showLimit) {
			referwfRAM.removeFirst();
		}
	}

	public void removefromRAMList(int dest) {
		ListIterator<IReferenceWaveForm> li = referwfRAM.listIterator();

		while (li.hasNext()) {
			IReferenceWaveForm rwf = li.next();
			if (rwf.getObjIndex() == dest) {
				li.remove();
				break;
			}
		}
	}

	public boolean isObjShowing(int showIdx) {
		// 多一个判断，用于：如果id相同的rwf已经存在rwflist，就return
		// 不要再删除然后重新添加进内存
		for (IReferenceWaveForm rwf : referwfRAM) {
			if (rwf.getObjIndex() == showIdx)
				return true;
		}
		return false;
	}

	public boolean show_info;
	public String name[] = new String[showLimit], tb[] = new String[showLimit],
			vb[] = new String[showLimit];

	public String updateSingleInfo(int selIdx, TimeConfProvider tcp,
			VoltageProvider vp, MathControl mc) {
		IReferenceWaveForm rwf = getShowReferenceWaveForm(selIdx);
		String name, tb, vb;
		name = getReferfileROM()[selIdx].getComplexName();
		if (rwf == null)
			return name + ":  ?";
		tb = tcp.getTimebaseLabel(rwf.getTbIdx());
		vb = rwf.getIntVoltageLabel_mV(vp, mc);
		return name + ":  " + vb + "  " + tb;
	}

	public void updateAllInfo(TimeConfProvider tcp, VoltageProvider vp,
			MathControl mc) {
		int i = 0;
		for (IReferenceWaveForm rwf : referwfRAM) {
			ReferenceFile f = getReferfileROM()[rwf.getObjIndex()];
			name[i] = f.getComplexName();
			tb[i] = tcp.getTimebaseLabel(rwf.getTbIdx());
			vb[i] = rwf.getIntVoltageLabel_mV(vp, mc);
			i++;
		}
	}

	public IReferenceWaveForm getShowReferenceWaveForm(int showIdx) {
		for (IReferenceWaveForm rwf : referwfRAM) {
			if (rwf.getObjIndex() == showIdx)
				return rwf;
		}
		return null;
	}

	/** 界面不会更新的参考波形清理 */
	public void clearAllshow() {
		referwfRAM.clear();
	}

	public void adjustReferenceView(ScreenContext pc, Rectangle bound) {
		for (IReferenceWaveForm rwf : referwfRAM) {
			rwf.adjustView(pc, bound);
		}
	}

	public void paitReferenceWaves(Graphics2D g2d, ScreenContext pc, Rectangle r) {
		for (IReferenceWaveForm rwf : referwfRAM) {
			rwf.paintView(g2d, pc, r);
		}
		paintRFLabel(g2d, pc);

	}

	private void paintRFLabel(Graphics2D g2d, ScreenContext pc) {
		Color tmp = g2d.getColor();
		int x = 120, y = 37, w = 110, h = 100, arc = 10;
		g2d.setColor(Color.PINK);
		show_info = !referwfRAM.isEmpty();
		if (show_info) {
			int size = referwfRAM.size();
			String info;
			for (int i = 0; i < size; i++) {
				info = name[i] + "  :   " + vb[i] + "   " + tb[i];
				FontMetrics fm = g2d.getFontMetrics();
				int len = fm.stringWidth(info);
				w = len > w ? len : w;
				g2d.drawString(info, x + 5, y);
				y += 20;
			}
			g2d.drawRoundRect(x, 20, w + 10, y - 37, arc, arc);
		}
		g2d.setColor(tmp);
	}

	public void paitReferenceItems(Graphics2D g2d, ScreenContext pc, Rectangle r) {
		for (IReferenceWaveForm rwf : referwfRAM) {
			rwf.paintItem(g2d, pc, r, false);
		}
	}

	public void load(Pref p) {
		setSourceIdx(p.loadInt("ReferenceSourceIdx"));
		if (getSourceIdx() >= cm.getSupportChannelsNumber())
			setSourceIdx(cm.getSupportChannelsNumber() - 1);
		useableList = p.loadIntegerList("ReferenceObjsUseable", ",");
		rfObjNames = p.loadStringList("ReferenceObjsNames", ",");
	}

	public void factoryload(Pref p) {
		load(p);
		clearAllshow();
	}

	public void persist(Pref p) {
		LinkedList<Integer> useable = new LinkedList<Integer>();
		List<String> names = new LinkedList<String>();
		for (int i = 0; i < getReferfileROM().length; i++) {
			if (getReferfileROM()[i].use) {
				useable.add(i);
			}
			String name = getReferfileROM()[i].getName();
			if (name.equalsIgnoreCase(""))
				names.add("0");
			else
				names.add(name);
		}
		p.persistIntegerList("ReferenceObjsUseable", useable, ",");
		p.persistStringList("ReferenceObjsNames", names, ",");
		p.persistInt("ReferenceSourceIdx", getSourceIdx());
	}

}
