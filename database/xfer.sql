INSERT INTO provider (action_code, last_updated, address_line_1, address_line_2, also_known_as, county, email, fax, in_process, is_partnership, latitude, location_authority, longitude, name, postcode, provider_id, region, telephone, town_city, type, under_review_text, website)
SELECT 
'I', now(),
providerprofile.provider_a.address1,
providerprofile.provider_a.address2,
providerprofile.provider_a.also_known_as,
providerprofile.provider_a.county,
providerprofile.provider_a.email,
providerprofile.provider_a.fax,
providerprofile.provider_a.in_process,
providerprofile.provider_a.is_partnership,
providerprofile.provider_a.latitude,
providerprofile.provider_a.provider_local_authority,
providerprofile.provider_a.longitude,
providerprofile.provider_a.provider_name,
providerprofile.provider_a.postcode,
providerprofile.provider_a.provider_id,
providerprofile.provider_a.provider_region,
providerprofile.provider_a.telephone,
providerprofile.provider_a.towncity,
providerprofile.provider_a.provider_type,
providerprofile.provider_a.under_review_text,
providerprofile.provider_a.website
FROM providerprofile.provider_a
LIMIT 100;

INSERT INTO location (about_location, action_code, last_updated, address_line_1, address_line_2, also_known_as, county, email, fax, in_process, latitude, local_authority, location_id, longitude, name, postcode, provider_id, region, statement_date, telephone, town_city, type, under_review_text, user_experience, website)
SELECT
'', 'I', now(),
providerprofile.location_a.address1,
providerprofile.location_a.address2,
providerprofile.location_a.also_known_as,
providerprofile.location_a.county,
providerprofile.location_a.email,
providerprofile.location_a.fax,
providerprofile.location_a.in_process,
providerprofile.location_a.latitude,
providerprofile.location_a.location_local_authority,
providerprofile.location_a.location_id,
providerprofile.location_a.longitude,
providerprofile.location_a.location_name,
providerprofile.location_a.postcode,
providerprofile.location_a.provider_id,
providerprofile.location_a.location_region,
providerprofile.location_a.location_statement_date,
providerprofile.location_a.telephone,
providerprofile.location_a.towncity,
providerprofile.location_a.location_type,
providerprofile.location_a.under_review_text,
providerprofile.location_a.location_user_experience,
providerprofile.location_a.website
FROM providerprofile.location_a
WHERE providerprofile.location_a.provider_id IN ("1-101604141","1-101604150","1-101604168","1-101604186","1-101604195","1-101606204","1-101606213","1-101606222","1-101606231","1-101606240","1-101606248","1-101606257","1-101606266","1-101606275","1-101606284","1-101606293","1-101606311","1-101606320","1-101606329","1-101606338","1-101606347","1-101606356","1-101606365","1-101606374","1-101606383","1-101606392","1-101606401","1-101606410","1-101606419","1-101606428","1-101606446","1-101606455","1-101606464","1-101606473","1-101606491","1-101606500","1-101606527","1-101606536","1-101606545","1-101606554","1-101606563","1-101606572","1-101606581","1-101606590","1-101606599","1-101606608","1-101606617","1-101606626","1-101606635","1-101606644","1-101606653","1-101606662","1-101606671","1-101606680","1-101606689","1-101606698","1-101607707","1-101607716","1-101607725","1-101607752","1-101607761","1-101607770","1-101607779","1-101607788","1-101607797","1-101607806","1-101607824","1-101607833","1-101607842","1-101607851","1-101607869","1-101607878","1-101607887","1-101607896","1-101607905","1-101607914","1-101607932","1-101607941","1-101607950","1-101607959","1-101607968","1-101607977","1-101607986","1-101607995","1-101608004","1-101608013","1-101608022","1-101608031","1-101608040","1-101608049","1-101608067","1-101608076","1-101608085","1-101608094","1-101608103","1-101608112","1-101608121","1-101608130","1-101608139","1-101608148");

