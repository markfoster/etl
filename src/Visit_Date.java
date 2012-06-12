// default package
// Generated 11-Jun-2012 16:41:24 by Hibernate Tools 3.2.2.GA


import java.util.Date;

/**
 * ServiceUserBand generated by hbm2java
 */
public class Visit_Date  implements java.io.Serializable {

     private String providerId;
     private String locationId;
     private String outcomeNumber;
     private String reportPublicationDate;
     private String visitDate;
     private Character actionCode;
     private Date lastUpdated;

    public Visit_Date() {
    }

    public Visit_Date(String providerId, String locationId, String outcomeNumber, String reportPublicationDate, String visitDate) {
       this.providerId = providerId;
       this.locationId = locationId;
       this.outcomeNumber = outcomeNumber;
       this.reportPublicationDate = reportPublicationDate;
       this.visitDate = visitDate;
    }

    public Visit_Date(String providerId, String locationId, String outcomeNumber, String reportPublicationDate, String visitDate, Character actionCode, Date lastUpdated) {
       this.providerId = providerId;
       this.locationId = locationId;
       this.outcomeNumber = outcomeNumber;
       this.reportPublicationDate = reportPublicationDate;
       this.visitDate = visitDate;
       this.actionCode = actionCode;
       this.lastUpdated = lastUpdated;
    }

    public String getProviderId() {
        return this.providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }
    public String getLocationId() {
        return this.locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getOutcomeNumber() {
        return this.outcomeNumber;
    }

    public void setOutcomeNumber(String outcomeNumber) {
        this.outcomeNumber = outcomeNumber;
    }

    public String getReportPublicationDate() {
        return this.reportPublicationDate;
    }

    public void setReportPublicationDate(String reportPublicationDate) {
        this.reportPublicationDate = reportPublicationDate;
    }

    public String getVisitDate() {
        return this.visitDate;
    }

    public void setVisitDate(String visitDate) {
        this.visitDate = visitDate;
    }

    public Character getActionCode() {
        return this.actionCode;
    }
    
    public void setActionCode(Character actionCode) {
        this.actionCode = actionCode;
    }
    public Date getLastUpdated() {
        return this.lastUpdated;
    }
    
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

   public boolean equals(Object other) {
         if ( (this == other ) ) return true;
                 if ( (other == null ) ) return false;
                 if ( !(other instanceof Visit_Date) ) return false;
                 Visit_Date castOther = ( Visit_Date ) other;

return ( (this.getProviderId()==castOther.getProviderId()) || ( this.getProviderId()!=null && castOther.getProviderId()!=null && this.getProviderId().equals(castOther.getProviderId()) ) )
    && ( (this.getLocationId()==castOther.getLocationId()) || ( this.getLocationId()!=null && castOther.getLocationId()!=null && this.getLocationId().equals(castOther.getLocationId()) ) )
    && ( (this.getOutcomeNumber()==castOther.getOutcomeNumber()) || ( this.getOutcomeNumber()!=null && castOther.getOutcomeNumber()!=null && this.getOutcomeNumber().equals(castOther.getOutcomeNumber()) ) )
    && ( (this.getReportPublicationDate()==castOther.getReportPublicationDate()) || ( this.getReportPublicationDate()!=null && castOther.getReportPublicationDate()!=null && this.getReportPublicationDate().equals(castOther.getReportPublicationDate()) ) )
    && ( (this.getVisitDate()==castOther.getVisitDate()) || ( this.getVisitDate()!=null && castOther.getVisitDate()!=null && this.getVisitDate().equals(castOther.getVisitDate()) ) );
   }

   public int hashCode() {
         int result = 17;

         result = 37 * result + ( getProviderId() == null ? 0 : this.getProviderId().hashCode() );
         result = 37 * result + ( getLocationId() == null ? 0 : this.getLocationId().hashCode() );
         result = 37 * result + ( getOutcomeNumber() == null ? 0 : this.getOutcomeNumber().hashCode() );
         result = 37 * result + ( getReportPublicationDate() == null ? 0 : this.getReportPublicationDate().hashCode() );
         result = 37 * result + ( getVisitDate() == null ? 0 : this.getVisitDate().hashCode() );
         return result;
   }

}


