/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type UpdateUserInfoRequest struct {
	FirstName         *string `json:"firstName,omitempty"`
	LastName          *string `json:"lastName,omitempty"`
	PhoneNumber       *string `json:"phoneNumber,omitempty"`
	AvatarUrl         *string `json:"avatarUrl,omitempty"`
	Timezone          *string `json:"timezone,omitempty"`
	PreferredLanguage *string `json:"preferredLanguage,omitempty"`
	KeycloakUserId    *string `json:"keycloakUserId,omitempty"`
}
