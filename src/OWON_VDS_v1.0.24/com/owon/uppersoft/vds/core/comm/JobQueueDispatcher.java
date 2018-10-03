package com.owon.uppersoft.vds.core.comm;

import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.owon.uppersoft.vds.core.comm.effect.JobUnitDealer;
import com.owon.uppersoft.vds.core.comm.job.JobUnit;

/**
 * DataFetcher，波形数据获取，本身只是一个将各工作环节连结起来的控制器
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

	// TODO 用堆指令无序有影响吗？，考虑使用queue
	private List<JobUnit> queue = Collections
			.synchronizedList(new LinkedList<JobUnit>());

	public boolean dealQueue() {
		/** 针对网络作sleep(0)? */
		synchronized (queue) {
			/**
			 * 通过加锁同步只能保证没有其它线程在调用指令下传的过程，但指令使用的数据仍然可由其它方改变的，并非线程安全，
			 * 可考虑用固定的p0,vb,tb,t0代替堆，暂缓进一步优化，这样可以兼容新加进去的固定JobUnit
			 */
			int num = queue.size();
			pcs.firePropertyChange(UPDATE_CMD_COUNTER, -1, num);
			if (num == 0)
				return false;

			/** 实现多线程互锁，防止在取元素的同时添加 */
			while (queue.size() > 0) {
				JobUnit p = queue.remove(0);
				// System.out.println(p.getName());
				p.doJob(sm);
			}
			return true;
		}
	}

	/**
	 * 非同步加锁地添加任务
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
	 * 添加指令任务，大部分指令，都不用即使生效
	 * 
	 * @param p
	 */
	public void addJobUnit(final JobUnit p) {
		synchronized (queue) {
			addQueueElement(p);
		}
	}

}