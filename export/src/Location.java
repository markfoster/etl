// default package
// Generated 11-Jun-2012 16:41:24 by Hibernate Tools 3.2.2.GA


import java.math.BigDecimal;
import java.util.Date;

/**
 * Location generated by hbm2java
 */
public class Location  implements java.io.Serializable {


     private LocationId id;
     private String aboutLocation;
     private Character actionCode;
     private String addressLine1;
     private String addressLine2;
     private String alsoKnownAs;
     private String county;
     private String email;
     private String fax;
     private String inProcess;
     private Date lastUpdated;
     private BigDecimal latitude;
     private String localAuthority;
     private BigDecimal longitude;
     private String name;
     private String postcode;
     private String region;
     private String segmentation;
     private Date statementDate;
     private String subtype;
     private String telephone;
     private String townCity;
     private String type;
     private String underReviewText;
     private String userExperience;
     private String website;

    public Location() {
    }

	
    public Location(LocationId id) {
        this.id = id;
    }
    public Location(LocationId id, String aboutLocation, Character actionCode, String addressLine1, String addressLine2, String alsoKnownAs, String county, String email, String fax, String inProcess, Date lastUpdated, BigDecimal latitude, String localAuthority, BigDecimal longitude, String name, String postcode, String region, String segmentation, Date statementDate, String subtype, String telephone, String townCity, String type, String underReviewText, String userExperience, String website) {
       this.id = id;
       this.aboutLocation = aboutLocation;
       this.actionCode = actionCode;
       this.addressLine1 = addressLine1;
       this.addressLine2 = addressLine2;
       this.alsoKnownAs = alsoKnownAs;
       this.county = county;
       this.email = email;
       this.fax = fax;
       this.inProcess = inProcess;
       this.lastUpdated = lastUpdated;
       this.latitude = latitude;
       this.localAuthority = localAuthority;
       this.longitude = longitude;
       this.name = name;
       this.postcode = postcode;
       this.region = region;
       this.segmentation = segmentation;
       this.statementDate = statementDate;
       this.subtype = subtype;
       this.telephone = telephone;
       this.townCity = townCity;
       this.type = type;
       this.underReviewText = underReviewText;
       this.userExperience = userExperience;
       this.website = website;
    }
   
    public LocationId getId() {
        return this.id;
    }
    
    public void setId(LocationId id) {
        this.id = id;
    }
    public String getAboutLocation() {
        return this.aboutLocation;
    }
    
    public void setAboutLocation(String aboutLocation) {
        this.aboutLocation = aboutLocation;
    }
    public Character getActionCode() {
        return this.actionCode;
    }
    
    public void setActionCode(Character actionCode) {
        this.actionCode = actionCode;
    }
    public String getAddressLine1() {
        return this.addressLine1;
    }
    
    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }
    public String getAddressLine2() {
        return this.addressLine2;
    }
    
    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }
    public String getAlsoKnownAs() {
        return this.alsoKnownAs;
    }
    
    public void setAlsoKnownAs(String alsoKnownAs) {
        this.alsoKnownAs = alsoKnownAs;
    }
    public String getCounty() {
        return this.county;
    }
    
    public void setCounty(String county) {
        this.county = county;
    }
    public String getEmail() {
        return this.email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    public String getFax() {
        return this.fax;
    }
    
    public void setFax(String fax) {
        this.fax = fax;
    }
    public String getInProcess() {
        return this.inProcess;
    }
    
    public void setInProcess(String inProcess) {
        this.inProcess = inProcess;
    }
    public Date getLastUpdated() {
        return this.lastUpdated;
    }
    
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    public BigDecimal getLatitude() {
        return this.latitude;
    }
    
    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }
    public String getLocalAuthority() {
        return this.localAuthority;
    }
    
    public void setLocalAuthority(String localAuthority) {
        this.localAuthority = localAuthority;
    }
    public BigDecimal getLongitude() {
        return this.longitude;
    }
    
    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    public String getPostcode() {
        return this.postcode;
    }
    
    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }
    public String getRegion() {
        return this.region;
    }
    
    public void setRegion(String region) {
        this.region = region;
    }
    public String getSegmentation() {
        return this.segmentation;
    }
    
    public void setSegmentation(String segmentation) {
        this.segmentation = segmentation;
    }
    public Date getStatementDate() {
        return this.statementDate;
    }
    
    public void setStatementDate(Date statementDate) {
        this.statementDate = statementDate;
    }
    public String getSubtype() {
        return this.subtype;
    }
    
    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }
    public String getTelephone() {
        return this.telephone;
    }
    
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
    public String getTownCity() {
        return this.townCity;
    }
    
    public void setTownCity(String townCity) {
        this.townCity = townCity;
    }
    public String getType() {
        return this.type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    public String getUnderReviewText() {
        return this.underReviewText;
    }
    
    public void setUnderReviewText(String underReviewText) {
        this.underReviewText = underReviewText;
    }
    public String getUserExperience() {
        return this.userExperience;
    }
    
    public void setUserExperience(String userExperience) {
        this.userExperience = userExperience;
    }
    public String getWebsite() {
        return this.website;
    }
    
    public void setWebsite(String website) {
        this.website = website;
    }




}


