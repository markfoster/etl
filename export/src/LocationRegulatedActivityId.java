// default package
// Generated 28-May-2012 14:25:40 by Hibernate Tools 3.2.2.GA



/**
 * LocationRegulatedActivityId generated by hbm2java
 */
public class LocationRegulatedActivityId  implements java.io.Serializable {


     private String providerId;
     private String locationId;
     private String regulatedActivityNumber;

    public LocationRegulatedActivityId() {
    }

    public LocationRegulatedActivityId(String providerId, String locationId, String regulatedActivityNumber) {
       this.providerId = providerId;
       this.locationId = locationId;
       this.regulatedActivityNumber = regulatedActivityNumber;
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
    public String getRegulatedActivityNumber() {
        return this.regulatedActivityNumber;
    }
    
    public void setRegulatedActivityNumber(String regulatedActivityNumber) {
        this.regulatedActivityNumber = regulatedActivityNumber;
    }


   public boolean equals(Object other) {
         if ( (this == other ) ) return true;
		 if ( (other == null ) ) return false;
		 if ( !(other instanceof LocationRegulatedActivityId) ) return false;
		 LocationRegulatedActivityId castOther = ( LocationRegulatedActivityId ) other; 
         
		 return ( (this.getProviderId()==castOther.getProviderId()) || ( this.getProviderId()!=null && castOther.getProviderId()!=null && this.getProviderId().equals(castOther.getProviderId()) ) )
 && ( (this.getLocationId()==castOther.getLocationId()) || ( this.getLocationId()!=null && castOther.getLocationId()!=null && this.getLocationId().equals(castOther.getLocationId()) ) )
 && ( (this.getRegulatedActivityNumber()==castOther.getRegulatedActivityNumber()) || ( this.getRegulatedActivityNumber()!=null && castOther.getRegulatedActivityNumber()!=null && this.getRegulatedActivityNumber().equals(castOther.getRegulatedActivityNumber()) ) );
   }
   
   public int hashCode() {
         int result = 17;
         
         result = 37 * result + ( getProviderId() == null ? 0 : this.getProviderId().hashCode() );
         result = 37 * result + ( getLocationId() == null ? 0 : this.getLocationId().hashCode() );
         result = 37 * result + ( getRegulatedActivityNumber() == null ? 0 : this.getRegulatedActivityNumber().hashCode() );
         return result;
   }   


}


