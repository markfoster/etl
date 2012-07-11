#!/bin/bash
#
# CMS0294Functions.sh - Standard shell functions
#
# $Id: CMS0294Functions.sh,v 1.4 2012/05/25 08:48:46 appadmin Exp $
#
#
#   Date	Version	Who	Description
#   ====	=======	===	===========
#
#   03/06/12	01.07	MF	Initial version
#
#

#
# Print/Usage functions
#

PrintInfo ()
{
    echo "$(date '+[%Y-%m-%d %H:%M:%S]') " "$*"
}

PrintVerboseInfo ()
{
    if   [ "${v_FLAG}" = "YES" ]
    then echo "$(date '+[%Y-%m-%d %H:%M:%S]') " "$*"
    fi
}

#
# Create a directory under the appropriate tree
#
# $1: Directory root
# $2: Directory name

CreateDirectory () {
    dn="${2}"

    if   [ ! -d "${1}/${dn}" ]
    then mkdir -pv "${1}/${dn}" 2>/dev/null

	 while [ "${dn}" != "" -a "${dn}" != "." -a "${dn}" != "/" ]
	 do
     	       chmod 755 "${1}/${dn}"
     	       chown "${USER_ID}" "${1}/${dn}"
	       dn=$(dirname "${dn}")
	 done
    fi
}

#
# Remove an empty directory under the appropriate tree
#
# $1: Directory root
# $2: Directory name

RemoveEmptyDirectory () {
    dn="${2}"

    if   [ -d "${1}/${dn}" ]
    then rmdir "${1}/${dn}" 2>/dev/null

	 while [ "${?}" -eq 0 -a "${dn}" != "" -a "${dn}" != "." -a "${dn}" != "/" ]
	 do
	       dn=$(dirname "${dn}")
	       rmdir "${1}/${dn}" 2>/dev/null
	 done
    fi
}

#
# Dump some files to standard out
#
# Parameters are: "File identifier" "filename" pairs
#

DumpFiles ()
{
    while [ "${1}" != "" ]
    do
	PrintInfo "${1} = ["
	shift
	cat "${1}"

	PrintInfo "]"
	shift

	echo
    done
}

DumpAudit ()
{
    audit_file=$(find ${XML_PROCESS_DIR} -type f -iname "pp_audit_xml*")
    echo "Audit = $audit_file"
    DumpFiles "Audit" $audit_file
}
