package com.owon.uppersoft.vds.core.comm;

import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.owon.uppersoft.vds.core.comm.effect.JobUnitDealer;
import com.owon.uppersoft.vds.core.comm.job.JobUnit;

/**
 * DataFetcher, waveform data acquisition, itself only a controller linking the work stages together
 * 
 * JobQueueDispatcher
 * 
 */
public class JobQueueDispatcher implements JobUnitDealer {
	public static final String UPDATE_CMD_COUNTER = "UPDATE_CMD_COUNTER";

	private BufferredSourceManager sm;
	private PropertyChangeSupport pcs;

	public JobQueueDispatcher(BufferredSourceManager sm,
			PropertyChangeSupport pcs) {
		this.sm = sm;
		this.pcs = pcs;
	}

	// TODO Does using a heap make command order matter?, consider using queue
	private List<JobUnit> queue = Collections
			.synchronizedList(new LinkedList<JobUnit>());

	public boolean dealQueue() {
		/** For the network, do sleep(0)? */
		synchronized (queue) {
			/**
			 * Locking for synchronization only guarantees that no other thread is in the process of calling command downlink, but the data used by the command can still be changed by others, so it is not thread-safe,
			 * Could consider using fixed p0,vb,tb,t0 instead of a heap; further optimization is deferred; this would be compatible with newly added fixed JobUnit
			 */
			int num = queue.size();
			pcs.firePropertyChange(UPDATE_CMD_COUNTER, -1, num);
			if (num == 0)
				return false;

			/** Implement multi-thread mutual locking, to prevent adding while elements are being taken */
			while (queue.size() > 0) {
				JobUnit p = queue.remove(0);
				// System.out.println(p.getName());
				p.doJob(sm);
			}
			return true;
		}
	}

	/**
	 * Add a task without synchronized locking
	 * 
	 * @param p
	 * @return
	 */
	private boolean addQueueElement(JobUnit p) {
		int size = queue.size();
		if (size > 0) {
			JobUnit lp = queue.get(queue.size() - 1);
			if (p.merge(lp)) {
				// System.err.println("Merger" + p.getName());
				return false;
			}
		}
		// System.err.println("add" + p.getName());
		queue.add(p);
		return true;
	}

	/**
	 * Add a command task; most commands do not need to take effect immediately
	 * 
	 * @param p
	 */
	public void addJobUnit(final JobUnit p) {
		synchronized (queue) {
			addQueueElement(p);
		}
	}

}