
-- Script to cleanup GUID database objects
-- Review script before uncommenting and running the script


drop database `jcvi_guid`;
drop user 'guid_client'@'%';
drop user 'guid_admin'@'%';


