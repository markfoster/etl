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

if  [ "${ENV_PATH1}" = "." ]
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

export XML_IN_DIR="/mnt/www/etl"
export XML_PROCESS_DIR="/var/tmp/etl/xml"

export COMMON_USER="cqcdms_m"
export COMMON_CRED="pcT1x5CXSDw"

export HIP_SOLR="hip_solr"
export HIP_WEBSITE="hip_web"
export HIP_REVISIONS="Revisions"
export HIP_BACKUP="hip_backup"

export HIP_ROOT="/data"						# HIP ROOT
export HIP_WROOT="${HIP_ROOT}/${HIP_WEBSITE}"			# Web Directory
export HIP_SROOT="${HIP_ROOT}/${HIP_SOLR}"			# Solr Indices Directory
export HIP_RROOT="${HIP_ROOT}/${HIP_REVISIONS}"			# Revisions Directory
export HIP_BROOT="${HIP_ROOT}/${HIP_BACKUP}"			# Backup Directory

export AKAMAI_USER="sshacs"					# HIP Akamai SFTP Account
export AKAMAI_HOST="hippoc.upload.akamai.com"			# HIP Akamai host
export AKAMAI_ROOT="/147209"					# HIP Akamai ROOT directory
export AKAMAI_PROTECTED="protected"				# HIP Akamai protected directory
export AKAMAI_PKI="TO BE DEFINED"

#export EVENT_MAIL="xansa.club.environmentmgmt@steria.co.uk"    # Alert Email address
export EVENT_MAIL="mark.foster@steria.co.uk"
export REPORT_MAIL="mark.foster@steria.co.uk"
export USER_ID="nginx:nginx"

# OAT Transfer Information

export OAT_HOST_UID="nginx"
export OAT_HOST_NAME="evl3300675.eu.verio.net"
export OAT_HOST_ROOT="/data"

# Epublish variables

export CLOUD_USER="${OAT_HOST_UID}"				# Cloud Environment SFTP account
export CLOUD_HOST="${OAT_HOST_NAME}"
export CLOUD_ROOT="${HIP_WROOT}"
export CLOUD_PKI="TO BE DEFINED"

#
# Health Check Info
#

export ETL_HC_HTML="ETLHealth.html"

# Install/Remove version Lock file

export LOCK_CONTROL="/tmp/CMS0294VersionControl"

# Upload Zip parameters

export FileSizeLimitM=100				# Minimum file size for not zipping
export ZipSizeLimitM=500				# Maximim size of files to be zipped.
