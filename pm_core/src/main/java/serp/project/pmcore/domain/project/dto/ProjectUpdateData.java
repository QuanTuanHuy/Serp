/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.dto;

public record ProjectUpdateData(
        String name,
        boolean nameProvided,
        String key,
        boolean keyProvided,
        String description,
        boolean descriptionProvided,
        Long leadUserId,
        boolean leadUserIdProvided,
        Long categoryId,
        boolean categoryIdProvided,
        String url,
        boolean urlProvided,
        Long avatarId,
        boolean avatarIdProvided
) {
}
