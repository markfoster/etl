//package org.cqc;
// Generated 24-May-2012 12:01:54 by Hibernate Tools 3.2.2.GA


import java.util.Date;

/**
 * Registered_Manager generated by hbm2java
 */
public class Registered_Manager  implements java.io.Serializable, CQC_Entity {

     private String name;
     private Character actionCode;
     private Date lastUpdated;

     private String providerId;
     private String locationId;
     private String registeredManagerId;
     private String regulatedActivityNumber;

    public Registered_Manager() {
    }

    public Registered_Manager(String providerId, String locationId, String registeredManagerId, String regulatedActivityNumber) {
       this.providerId = providerId;
       this.locationId = locationId;
       this.registeredManagerId = registeredManagerId;
       this.regulatedActivityNumber = regulatedActivityNumber;
    }

    public Registered_Manager(String providerId, String locationId, String registeredManagerId, String regulatedActivityNumber, String name,  Character actionCode, Date lastUpdated) {
       this.providerId = providerId;
       this.locationId = locationId;
       this.registeredManagerId = registeredManagerId;
       this.regulatedActivityNumber = regulatedActivityNumber;
       this.name = name;
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

    public String getRegisteredManagerId() {
        return this.registeredManagerId;
    }

    public void setRegisteredManagerId(String registeredManagerId) {
        this.registeredManagerId = registeredManagerId;
    }
    public String getRegulatedActivityNumber() {
        return this.regulatedActivityNumber;
    }

    public void setRegulatedActivityNumber(String regulatedActivityNumber) {
        this.regulatedActivityNumber = regulatedActivityNumber;
    }

    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
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
                 if ( !(other instanceof Registered_Manager) ) return false;
                 Registered_Manager castOther = ( Registered_Manager ) other;

                 return ( (this.getProviderId()==castOther.getProviderId()) || ( this.getProviderId()!=null && castOther.getProviderId()!=null && this.getProviderId().equals(castOther.getProviderId()) ) )
 && ( (this.getLocationId()==castOther.getLocationId()) || ( this.getLocationId()!=null && castOther.getLocationId()!=null && this.getLocationId().equals(castOther.getLocationId()) ) )
 && ( (this.getRegisteredManagerId()==castOther.getRegisteredManagerId()) || ( this.getRegisteredManagerId()!=null && castOther.getRegisteredManagerId()!=null && this.getRegisteredManagerId().equals(castOther.getRegisteredManagerId()) ) )
 && ( (this.getRegulatedActivityNumber()==castOther.getRegulatedActivityNumber()) || ( this.getRegulatedActivityNumber()!=null && castOther.getRegulatedActivityNumber()!=null && this.getRegulatedActivityNumber().equals(castOther.getRegulatedActivityNumber()) ) );
   }

   public int hashCode() {
         int result = 17;

         result = 37 * result + ( getProviderId() == null ? 0 : this.getProviderId().hashCode() );
         result = 37 * result + ( getLocationId() == null ? 0 : this.getLocationId().hashCode() );
         result = 37 * result + ( getRegisteredManagerId() == null ? 0 : this.getRegisteredManagerId().hashCode() );
         result = 37 * result + ( getRegulatedActivityNumber() == null ? 0 : this.getRegulatedActivityNumber().hashCode() );
         return result;
   }

   public String getPK() {
         return String.format("%s/%s/%s/%s", getProviderId(), 
                                          getLocationId(), 
                                          getRegisteredManagerId(), 
                                          getRegulatedActivityNumber());
   }

}


