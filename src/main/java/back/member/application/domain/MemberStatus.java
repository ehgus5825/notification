package back.member.application.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MemberStatus {

    STEADY_STATUS("정상", "ROLE_STEADY_STATUS"),
    DORMANT_STATUS("휴면", "ROLE_DORMANT_STATUS"),
    BLOCK_STATUS("차단", "ROLE_BLOCK_STATUS"),
    LEAVE_STATUS("탈퇴", "ROLE_LEAVE_STATUS"),
    ADMIN("관리자", "ROLE_ADMIN");

    private String statusName;
    private String statusCode;
}
