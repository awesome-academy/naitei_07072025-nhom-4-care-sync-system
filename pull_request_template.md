## ✅ Checklist tự review pull trước khi ready để trainer review (Java Spring Boot)
- [ ] Sử dụng **thụt lề 4 spaces** đồng nhất ở tất cả các files `.java`, `.xml`, `.html`, `.properties`, v.v. (cấu hình lại IntelliJ/VSCode nếu chưa setup).
- [ ] Cuối mỗi file cần có **end line** (`\n`) để tránh lỗi đỏ khi diff trên Git.
- [ ] Mỗi dòng nếu quá dài thì cần xuống dòng (tối đa **100 ký tự/dòng**, cài đặt wrap line trong IDE nếu cần).
- [ ] `.gitignore` các file nhạy cảm (VD: `application-secret.properties`, `.env`, `/target/`, `.log`, `.class`, ...).
- [ ] Tham khảo và tuân theo Java Coding Convention:
  - https://google.github.io/styleguide/javaguide.html
  - hoặc https://www.oracle.com/java/technologies/javase/codeconventions-contents.html
- [ ] Kiểm tra mỗi pull request **chỉ có 1 commit**, nếu nhiều hơn hãy dùng `git rebase -i` để gộp commit.
- [ ] Cài đặt `Checkstyle`, `PMD`, hoặc plugin Java linter tương ứng trong IDE để kiểm tra coding convention. Format lại trước khi gửi pull.
- [ ] Nếu làm việc nhóm, mỗi pull cần **ít nhất 1 APPROVED** từ thành viên khác trong nhóm.

---
## Related Tickets
- [#TicketID](https://edu-redmine.sun-asterisk.vn/issues/???)

## WHAT (optional)
- Change number items `completed/total` in admin page.

## HOW
- I edit js file, inject not_vary_normal items in calculate function.

## WHY (optional)
- Because in previous version - number just depends on `normal` items. But in new version, we have `state` and `confirm_state` depends on both `normal` + `not_normal` items.

## Evidence (Screenshot or Video)
