# groupDeviceCount
## Introduction
The Group Device Count DataSource utilizes the LogicMonitor API to query device group folders for their respective device
counts.  These device group folders exist as instances.  The DataSource then uses the API to update instance level alert
thresholds to trigger alerts based on a user defined device limit, which is defined as a device group level property.

## Setup
### Device Properties
In order to reduce the number of API calls made during collection, it is recommended that this DataSource be applied to a 
**SINGLE** device.  The user must define the following device properties for the device that the DataSource is applied to:

* lmaccess.id
* lmaccess.key
* lmaccount

The lmaccess.id and lmaccess.key correspond to a user's API credentials. The lmaccount refers to the ### in a customer's 
###.logicmonitor.com URL.

### Group Level Properties
In addition, the user must apply the following group level property to device groups they would like to monitor:

* device_limit

This property is used to dynamically update the instance level alert threshhold for the "device_count" datapoint of the
corresponding instance.
