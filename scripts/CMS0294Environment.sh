#!/bin/bash
#
# CMS0294Environment.sh - Set up Environment files
#
# $Id: CMS0294Environment.sh,v 1.12 2012/05/17 14:08:10 appadmin Exp $
#
#
#   Date	Version	Who	Description
#   ====	=======	===	===========
#
#   01/06/12	01.00	MF	Initial version
#

# Load the functions
#
# Find our commands
#

ENV_PATH=$(dirname "${0}")

if  [ "${ENV_PATH}" = "." ]
then ENV_PATH="${PWD}"
elif [ "${ENV_PATH}" = ".." ]
then ENV_PATH="${PWD}/.."
fi

# Load the environment parameters

if   [ -f "${ENV_PATH}/CMS0294Functions.sh" ]
then . "${ENV_PATH}/CMS0294Functions.sh"
elif [ -f "/usr/local/bin/CMS0294Functions.sh" ]
then . "/usr/local/bin/CMS0294Functions.sh"
else echo "CMS0294Environment.sh:  Missing functions file." 2>&1
     exit 1
fi

# Initialise Parameters
#
# Make sure you export them!!!

export LOCKS_DIR="/mnt/www/locks"

export BACKUP_DIR="/mnt/www/backups"

export XML_IN_DIR="/mnt/www/etl"
export XML_PROCESS_DIR="/opt/etl/xml"

export COMMON_HOST="10.38.1.65"
export COMMON_DB="common"
export COMMON_USER="cqcdms_m"
export COMMON_CRED="pcT1x5CXSDw"

export PROD_HOST="10.38.1.65"
export PROD_DB="production_pp"
export PROD_USER="cqcdms_p"
export PROD_CRED="ncZ1x6CWSDa"

export JAVA_OPTS="-Xms512m -Xmx2048m"

#export EVENT_MAIL="xansa.club.environmentmgmt@steria.co.uk"    # Alert Email address
export EVENT_MAIL="mark.foster@steria.co.uk,pratibha.seth9521@steria.co.in,chi@hausolutions.com,jennifer.jordan@steria.co.uk"
export MAIL_FROM="CMS0294Alert@steria.co.uk"
#export EVENT_MAIL="mark.foster@steria.co.uk"
export REPORT_MAIL="mark.foster@steria.co.uk,pratibha.seth9521@steria.co.in,chi@hausolutions.com,jennifer.jordan@steria.co.uk"
#export REPORT_MAIL="mark.foster@steria.co.uk"
export REPORT_MAIL_FROM="CMS0294Report@steria.co.uk"

#
# Health Check Info
#
export ETL_HC_HTML="ETLHealth.html"

# Install/Remove version Lock file

export LOCK_CONTROL="/tmp/CMS0294VersionControl"

# Upload Zip parameters

export FileSizeLimitM=100				# Minimum file size for not zipping
export ZipSizeLimitM=500				# Maximim size of files to be zipped.

export AuditTimeOut=2
export FileStabilityTimeOut=2
export LockTimeOut=1
export SizeLimit=5000
export OfficeHoursStart=2
export OfficeHoursEnd=4


