// default package
// Generated 11-Jun-2012 16:41:24 by Hibernate Tools 3.2.2.GA


import java.util.Date;

/**
 * Chapter generated by hbm2java
 */
public class Chapter  implements java.io.Serializable, CQC_Entity {

     private String providerId;
     private String locationId;
     private String chapterNumber;
     private Integer score;
     private Character actionCode;
     private Date lastUpdated;

    public Chapter() {
    }

	
    public Chapter(String providerId, String locationId, String chapterNumber) {
       this.providerId = providerId;
       this.locationId = locationId;
       this.chapterNumber = chapterNumber;
    }

    public Chapter(String providerId, String locationId, String chapterNumber, Integer score, Character actionCode, Date lastUpdated) {
       this.providerId = providerId;
       this.locationId = locationId;
       this.chapterNumber = chapterNumber;
       this.score = score;
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
    public String getChapterNumber() {
        return this.chapterNumber;
    }

    public void setChapterNumber(String chapterNumber) {
        this.chapterNumber = chapterNumber;
    }
 
    public Integer getScore() {
        return this.score;
    }
    
    public void setScore(Integer score) {
        this.score = score;
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
                 if ( !(other instanceof Chapter) ) return false;
                 Chapter castOther = ( Chapter ) other;

                 return ( (this.getProviderId()==castOther.getProviderId()) || ( this.getProviderId()!=null && castOther.getProviderId()!=null && this.getProviderId().equals(castOther.getProviderId()) ) )
 && ( (this.getLocationId()==castOther.getLocationId()) || ( this.getLocationId()!=null && castOther.getLocationId()!=null && this.getLocationId().equals(castOther.getLocationId()) ) )
 && ( (this.getChapterNumber()==castOther.getChapterNumber()) || ( this.getChapterNumber()!=null && castOther.getChapterNumber()!=null && this.getChapterNumber().equals(castOther.getChapterNumber()) ) );
   }

   public int hashCode() {
         int result = 17;

         result = 37 * result + ( getProviderId() == null ? 0 : this.getProviderId().hashCode() );
         result = 37 * result + ( getLocationId() == null ? 0 : this.getLocationId().hashCode() );
         result = 37 * result + ( getChapterNumber() == null ? 0 : this.getChapterNumber().hashCode() );
         return result;
   }

   public String getPK() {
         return String.format("%s/%s/%s", getProviderId(), getLocationId(), getChapterNumber());
   }

}


