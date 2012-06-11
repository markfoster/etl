// default package
// Generated 11-Jun-2012 16:41:24 by Hibernate Tools 3.2.2.GA



/**
 * RegisteredManagerConditionId generated by hbm2java
 */
public class RegisteredManagerConditionId  implements java.io.Serializable {


     private String providerId;
     private String locationId;
     private String registeredManagerId;
     private String regulatedActivityNumber;
     private String conditionId;

    public RegisteredManagerConditionId() {
    }

    public RegisteredManagerConditionId(String providerId, String locationId, String registeredManagerId, String regulatedActivityNumber, String conditionId) {
       this.providerId = providerId;
       this.locationId = locationId;
       this.registeredManagerId = registeredManagerId;
       this.regulatedActivityNumber = regulatedActivityNumber;
       this.conditionId = conditionId;
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
    public String getConditionId() {
        return this.conditionId;
    }
    
    public void setConditionId(String conditionId) {
        this.conditionId = conditionId;
    }


   public boolean equals(Object other) {
         if ( (this == other ) ) return true;
		 if ( (other == null ) ) return false;
		 if ( !(other instanceof RegisteredManagerConditionId) ) return false;
		 RegisteredManagerConditionId castOther = ( RegisteredManagerConditionId ) other; 
         
		 return ( (this.getProviderId()==castOther.getProviderId()) || ( this.getProviderId()!=null && castOther.getProviderId()!=null && this.getProviderId().equals(castOther.getProviderId()) ) )
 && ( (this.getLocationId()==castOther.getLocationId()) || ( this.getLocationId()!=null && castOther.getLocationId()!=null && this.getLocationId().equals(castOther.getLocationId()) ) )
 && ( (this.getRegisteredManagerId()==castOther.getRegisteredManagerId()) || ( this.getRegisteredManagerId()!=null && castOther.getRegisteredManagerId()!=null && this.getRegisteredManagerId().equals(castOther.getRegisteredManagerId()) ) )
 && ( (this.getRegulatedActivityNumber()==castOther.getRegulatedActivityNumber()) || ( this.getRegulatedActivityNumber()!=null && castOther.getRegulatedActivityNumber()!=null && this.getRegulatedActivityNumber().equals(castOther.getRegulatedActivityNumber()) ) )
 && ( (this.getConditionId()==castOther.getConditionId()) || ( this.getConditionId()!=null && castOther.getConditionId()!=null && this.getConditionId().equals(castOther.getConditionId()) ) );
   }
   
   public int hashCode() {
         int result = 17;
         
         result = 37 * result + ( getProviderId() == null ? 0 : this.getProviderId().hashCode() );
         result = 37 * result + ( getLocationId() == null ? 0 : this.getLocationId().hashCode() );
         result = 37 * result + ( getRegisteredManagerId() == null ? 0 : this.getRegisteredManagerId().hashCode() );
         result = 37 * result + ( getRegulatedActivityNumber() == null ? 0 : this.getRegulatedActivityNumber().hashCode() );
         result = 37 * result + ( getConditionId() == null ? 0 : this.getConditionId().hashCode() );
         return result;
   }   


}


