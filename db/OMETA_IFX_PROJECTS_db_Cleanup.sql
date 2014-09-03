
/*Script to cleanup OMETA database objects for ifx_projects */
/*Review script before uncommenting and running the script*/

/*--Cleanup command to ifx_projects database*/

drop database `ifx_projects`;
drop user 'ifx_projects_app'@'%';
drop user 'ifx_projects_adm'@'%';

