# deviceGroupLimiter
## Introduction
The Group Device Count DataSource utilizes the LogicMonitor API to query device group folders for their respective device
counts.  These device group folders exist as instances.  The DataSource then uses the API to update instance level alert
thresholds to trigger alerts based on a user defined device limit, which is defined as a device group level property.

### Please Note
### ___________
