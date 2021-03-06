// default package
// Generated 11-Jun-2012 16:41:24 by Hibernate Tools 3.2.2.GA



/**
 * ServiceUserBandId generated by hbm2java
 */
public class ServiceUserBandId  implements java.io.Serializable {


     private String providerId;
     private String locationId;
     private String serviceUserBandId;

    public ServiceUserBandId() {
    }

    public ServiceUserBandId(String providerId, String locationId, String serviceUserBandId) {
       this.providerId = providerId;
       this.locationId = locationId;
       this.serviceUserBandId = serviceUserBandId;
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
    public String getServiceUserBandId() {
        return this.serviceUserBandId;
    }
    
    public void setServiceUserBandId(String serviceUserBandId) {
        this.serviceUserBandId = serviceUserBandId;
    }


   public boolean equals(Object other) {
         if ( (this == other ) ) return true;
		 if ( (other == null ) ) return false;
		 if ( !(other instanceof ServiceUserBandId) ) return false;
		 ServiceUserBandId castOther = ( ServiceUserBandId ) other; 
         
		 return ( (this.getProviderId()==castOther.getProviderId()) || ( this.getProviderId()!=null && castOther.getProviderId()!=null && this.getProviderId().equals(castOther.getProviderId()) ) )
 && ( (this.getLocationId()==castOther.getLocationId()) || ( this.getLocationId()!=null && castOther.getLocationId()!=null && this.getLocationId().equals(castOther.getLocationId()) ) )
 && ( (this.getServiceUserBandId()==castOther.getServiceUserBandId()) || ( this.getServiceUserBandId()!=null && castOther.getServiceUserBandId()!=null && this.getServiceUserBandId().equals(castOther.getServiceUserBandId()) ) );
   }
   
   public int hashCode() {
         int result = 17;
         
         result = 37 * result + ( getProviderId() == null ? 0 : this.getProviderId().hashCode() );
         result = 37 * result + ( getLocationId() == null ? 0 : this.getLocationId().hashCode() );
         result = 37 * result + ( getServiceUserBandId() == null ? 0 : this.getServiceUserBandId().hashCode() );
         return result;
   }   


}


