package com.owon.uppersoft.vds.source.comm;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;
import java.util.List;

import com.owon.uppersoft.dso.source.usb.USBDelegate;
import com.owon.uppersoft.dso.source.usb.USBPortsFilter;
import com.owon.uppersoft.dso.source.usb.USBSource;
import com.owon.uppersoft.vds.core.usb.IDevice;
import com.owon.uppersoft.vds.util.format.EndianUtil;
import com.owon.vds.firm.protocol.AddValueAttachCommand;

public abstract class PortFilterTiny implements USBPortsFilter {
	public static final AddValueAttachCommand MACHINE_TYPE_ADD = new AddValueAttachCommand(
			"MACHINE_TYPE_ADD", 0x4001, 1, 'V');

	@Override
	public List<IDevice> collectPorts(List<IDevice> udl, USBDelegate usbsm) {
		Iterator<IDevice> it = udl.iterator();
		while (it.hasNext()) {
			IDevice id = it.next();
			String iSerialNumber = id.getUsbDevice().getSerialNumber();

			String machineName = "";
			if (!isMachineName(iSerialNumber)) {
				ByteBuffer bb = ByteBuffer.allocate(6);
				bb.order(ByteOrder.LITTLE_ENDIAN);

				AddValueAttachCommand mta = MACHINE_TYPE_ADD;
				bb.putInt(mta.address);
				bb.put((byte) mta.bytes);
				bb.put((byte) mta.value);

				boolean canUse = false;
				USBSource usbs = usbsm._getUSBSource(id);
				if (usbs != null) {
					usbs.write(bb.array(), 6);
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					// 'V'+4bytes 1
					int n = 5;
					byte[] arr = new byte[n];
					// System.out.println("sm.acceptResponse(");
					int rn = usbs.acceptResponse(arr, n);
					if (rn >= n) {
						int machineCode = EndianUtil.nextIntL(arr, 1);
						System.out.println("machineCode: " + machineCode);
						canUse = (machineCode > 0);
						machineName = getMachineNameFromCode(machineCode);
					}
					usbsm.releaseUSBSource(usbs);
				}
				if (!canUse || machineName == null) {
					it.remove();
					continue;
				} else {
					iSerialNumber = machineName;
				}
			}
			// System.out.println(iSerialNumber);
			id.setSerialNumber(iSerialNumber);
			rehandleIDevice(id);
		}
		return udl;
	}

	/**
	 * @param machineCode
	 * @return null means unrecognized
	 */
	protected abstract String getMachineNameFromCode(int machineCode);

	/**
	 * Some devices report a placeholder such as "0" as their USB serial
	 * number, they are asked for their machine code instead
	 *
	 * @param serialNumber
	 *            USB serial number
	 * @return true if the serial number can be used as the machine name
	 */
	protected boolean isMachineName(String serialNumber) {
		return serialNumber != null
				&& serialNumber.toUpperCase().startsWith("VDS");
	}

	/**
	 * can be used to modify the model description at the display stage
	 * 
	 * @param id
	 */
	protected void rehandleIDevice(IDevice id) {
	}
}