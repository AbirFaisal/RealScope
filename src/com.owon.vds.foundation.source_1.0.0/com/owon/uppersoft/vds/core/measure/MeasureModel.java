package com.owon.uppersoft.vds.core.measure;

import java.util.LinkedList;
import java.util.List;

import com.owon.uppersoft.vds.ui.widget.custom.CCheckBox;
import com.owon.uppersoft.vds.util.Pref;

public class MeasureModel {

	public static final String Refresh_MeasurePane_Selected = "refresh_MeasurePane_Selected";

	private final int columnMax = 7;
	private int channelNumbers;
	public LinkedList<MeasureElem> othMTlinked = new LinkedList<MeasureElem>();
	public LinkedList<Integer> MTlinked = new LinkedList<Integer>();
	private LinkedList<Integer> CHlinked = new LinkedList<Integer>();

	public MeasureModel(int chnum, Pref p) {
		channelNumbers = chnum;
		load(p);
	}

	public boolean isCHlinkEmpty() {
		return CHlinked.isEmpty();
	}

	public boolean isMTlinkEmpty() {
		return MTlinked.isEmpty();
	}

	private boolean isOthlinkEmpty() {
		return getOthLinkedCount() == 0;
	}

	public int getOthLinkedCount() {
		int on = 0;
		for (MeasureElem me : othMTlinked) {
			if (me.on)
				on++;
		}
		return on;
	}

	public int getColumnMax() {
		return isOthlinkEmpty() ? columnMax : columnMax - 1;
	}

	public int getColumnCount() {
		return MTlinked.size();
	}

	public void persist(Pref p) {
		p.persistIntegerList("MeasureChannels", CHlinked, ",");
		p.persistIntegerList("MeasureTypes", MTlinked, ",");
		p.persistMeasureElem(othMTlinked);
	}

	public void load(Pref p) {
		CHlinked = p.loadIntegerList("MeasureChannels", ",");
		MTlinked = p.loadIntegerList("MeasureTypes", ",");
		LinkedList<Integer> measureDelayCode = p.loadIntegerList(
				"MeasureDelayCode", ",");
		assembleOthMeasureQueue(measureDelayCode);
	}

	private void assembleOthMeasureQueue(LinkedList<Integer> measureDelayCode) {
		List<String> delayLableQueue = new LinkedList<String>();
		List<String> delayNameQueue = new LinkedList<String>();
		if (channelNumbers >= 2) {
			delayLableQueue.add("AutoMeasure.EdgeDelay1->2");
			delayLableQueue.add("AutoMeasure._EdgeDealy1->2");
			delayNameQueue.add("RDELAY12");
			delayNameQueue.add("FDELAY12");
		}
		if (channelNumbers >= 4) {
			delayLableQueue.add("AutoMeasure.EdgeDelay3->4");
			delayLableQueue.add("AutoMeasure._EdgeDealy3->4");
			delayNameQueue.add("RDELAY34");
			delayNameQueue.add("FDELAY34");
		}
		othMTlinked.clear();
		int len = delayLableQueue.size();
		for (int i = 0; i < len; i++) {
			MeasureElem me = new MeasureElem(i);
			me.on = false;
			if (measureDelayCode.contains(i))
				me.on = true;
			me.label = delayLableQueue.get(i);
			me.name = delayNameQueue.get(i);
			me.Value = 0;
			me.vu = "?";
			othMTlinked.add(me);
		}
	}

	public boolean enforcePermit() {
		boolean permissive = false;
		int mt = getColumnCount();
		int omt = getOthLinkedCount();
		if (mt > 0 || omt > 0)
			permissive = true;
		return permissive;

	}

	public void addChElem(int ch) {
		if (!CHlinked.contains(ch))
			CHlinked.add(ch);
	}

	public void delChElem(int ch) {
		if (CHlinked.contains(ch))
			CHlinked.remove((Integer) ch);
	}

	public void addMtElem(int p) {
		if (MTlinked.contains(p))
			return;
		if (MTlinked.size() >= getColumnMax()) {
			MTlinked.removeFirst();
		}
		MTlinked.add(p);
	}

	public boolean delMtElem(Integer p) {
		if (MTlinked.contains(p)) {
			MTlinked.remove(p);
			return true;
		}
		return false;
	}

	public void moveMtElem(int source, int dest) {
		if (source == dest || dest < 0 || source < 0)
			return;
		Integer elem = MTlinked.get(source);
		if (source > dest) {
			MTlinked.add(dest, elem);
			MTlinked.remove(++source);
		} else {
			MTlinked.add(++dest, elem);
			MTlinked.remove(source);
		}
	}

	public double getDelayValue(String cmd, int chl) {
		String suffix = chl < 2 ? "12" : "34";
		cmd = (cmd + suffix).toUpperCase();
		for (MeasureElem me : othMTlinked) {
			String n = me.name.toUpperCase();
			if (cmd.equalsIgnoreCase(n)) {
				return me.Value;
			}
		}
		return -1;
	}

	public boolean addOrDelDelay(boolean select, String cmd) {
		boolean b = false;
		cmd = cmd.toUpperCase();
		for (int i = 0; i < othMTlinked.size(); i++) {
			String ename = othMTlinked.get(i).name.toUpperCase();
			if (ename.startsWith(cmd)) {
				b = selectDelay(select, i);
				if (!b)
					break;
			}
		}
		return b;
	}

	public boolean selectDelay(boolean select, int idx) {
		int size = othMTlinked.size();
		if (idx < 0 || idx > size - 1)
			return false;
		MeasureElem delay = othMTlinked.get(idx);
		if (delay == null)
			return false;
		if (select && MTlinked.size() >= columnMax) {
			MTlinked.removeLast();
		}
		delay.on = select;
		return true;
	}

	public void removeAllMeasures() {
		CHlinked.clear();
		MTlinked.clear();
		for (MeasureElem me : othMTlinked) {
			me.on = false;
		}
	}

	public void updateMeasureSelected(CCheckBox[] chcb, CCheckBox[] mtcb,
			CCheckBox[] othmtcb) {
		for (int i = 0; i < chcb.length; i++) {
			boolean b = CHlinked.contains(i);
			chcb[i].setSelected(b);
		}
		for (int i = 0; i < mtcb.length; i++) {
			boolean b = MTlinked.contains(i);
			mtcb[i].setSelected(b);
		}
		// if (othmtcb != null)
		for (MeasureElem me : othMTlinked) {
			othmtcb[me.idx].setSelected(me.on);
		}

	}

	public void removeSelCol(int sel) {
		if (sel >= 0 && sel < getColumnCount()) {
			MTlinked.get(sel);
			MTlinked.remove(sel);
		}
	}

	public String getCHlinkElem() {
		StringBuilder sb = new StringBuilder();
		if (CHlinked.isEmpty()) {
			sb.append("CHANNEL ALL CLOSE");
			return sb.toString();
		}
		for (Integer e : CHlinked) {
			if (e < channelNumbers)
				sb.append("CH" + (e + 1) + ' ');
		}
		return sb.toString();
	}

	public boolean hasCHlinkElem(Integer i) {
		return CHlinked.contains(i);
	}

	@Deprecated
	private void printMTlink() {
		for (Integer e : MTlinked) {
			System.out.print("," + e);
		}
		System.out.println();
	}

	@Deprecated
	public boolean addOrDelRDelay(boolean select) {
		boolean b = false;
		b = selectDelay(select, 0);
		if (othMTlinked.size() >= 4)
			b = b && selectDelay(select, 2);
		return b;
	}

	@Deprecated
	public boolean addOrDelFDelay(boolean select) {
		boolean b = false;
		b = selectDelay(select, 1);
		if (othMTlinked.size() >= 4)
			b = b && selectDelay(select, 3);
		return b;
	}

	@Deprecated
	public Integer getMeasureTypeElem(int idx) {
		if (idx >= 0 && idx < MTlinked.size())
			return MTlinked.get(idx);
		return null;
	}

}
