
/*Script to cleanup OMETA database objects for dod_ometa */
/*Review script before uncommenting and running the script*/

/*--Cleanup command to dod_ometa database*/

drop database `dod_ometa`;
drop user 'dod_ometa_app'@'%';
drop user 'dod_ometa_adm'@'%';

