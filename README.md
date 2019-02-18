# CD2H-driveTagLib
Java library supporting integration access for the CD2H Google Drive hierarchy.

This code provides the glue to integrate various data in the CD2H Google Drive hierarchy (primarily in spreadsheets) with
our GitHub repositories.  Data are staged into a stand-off PostgreSQL database to allow decoupling of the polling for Drive
content and the serving of that data to consuming sites.
