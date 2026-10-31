# 검증 기록

검증일: 2026-10-31. 대상: Redstone Block Description 0.4.0, Minecraft Java 26.2,
Fabric Loader 0.19.5, Fabric API 0.160.0+26.2, Java 25, Windows.

## 결과

- `gradlew --offline build runClientGameTest`: **BUILD SUCCESSFUL**.
- `gradlew --offline runClientGameTest -PcaptureIllustrations -PcaptureExpanded`: **BUILD SUCCESSFUL**. 별도 게임 월드에서 새 장면 38장을 촬영했으며 전체 640×360 PNG는 140장입니다.
- 새 촬영 결과와 모드 리소스로 복사한 38장의 SHA-256 일치 확인.
- 전체 200개 등록 아이템의 실제 게임 레지스트리 존재 및 기본·상세 툴팁 연결 확인. 아이템이 있는 비교기 출력 블록에 설명이 누락되지 않았는지 검사 통과.
- 한국어·영어 리소스 각각 439개 키, 양쪽 키 일치 확인.
- 한국어·영어 61개 주제에서 기능·작동 조건·수치 위주로 검토.
- 양 언어에서 모든 설명의 번역 키 해석과 최대 250 GUI 픽셀 줄바꿈 검사 통과.
- Shift 누르기 및 해제로 상세·기본 표시 전환 확인.
- 실제 마우스 휠 입력으로 긴 Shift 설명이 스크롤되는지, 끝에서 멈추는지, 다른 아이템 및 Shift 해제 후 처음으로 돌아오는지 검사 통과.
- 기본 설명과 세부 정보 사이의 여백 검사 통과. 사진은 Shift 설명에 계속 포함됩니다.
- 동력·활성화 레일의 양방향 8칸 전원 전달과 일반 레일에 의한 단절, 20종 악기·추가 재료·25개 음높이 검사 통과.
- 감지체 반경·활성화 시간, 구리 전구 밝기, 상자 출력 수치 검사 통과. [정보 확인 기록](FACT_CHECK.md) 참고.
- 수량표의 18개 조건 조합, 총 8,037개 수량을 게임의 실제 계산과 대조했습니다. 도달할 수 없는 출력의 `—` 표시와 양조기 9개 구성 예시도 검사 통과.
- 선반의 점유 조합과 방향, 책장의 마지막 칸 번호 유지, 가마솥·케이크·정박기·동상 출력 검사 통과.
- 실제 발사기·공급기에 물 양동이를 넣어 블록 배치와 아이템 배출의 차이 검사 통과.
- 한국어·영어 표 머리글과 수량이 열 너비 안에 들어가는지 검사 통과. 27·54칸 상자, 호퍼, 양조기, 긴 발사기 설명, 선반·가마솥과 854×480 창을 실제 화면에서 검토.
- 상자류·덫 상자·독서대·퇴비통·주크박스의 기존 설명 문장이 이전 버전과 동일한지 양쪽 언어를 대조했습니다.
- 전체 설명 주제의 사진 파일·상태 번역·이미지 렌더러 연결, Shift를 누를 때만 사진 표시 확인.
- 1.8초 간격 슬라이드 전환, 주제 변경·마우스 이탈 후 첫 사진으로 돌아오는 동작 검사 통과.
- 비교기의 비교 모드와 빼기 모드 사진이 실제 화면에서 전환되는 것을 스크린샷으로 확인.
- 새 촬영 과정에서 감지 레일·덫 상자·주크박스의 조명 출력, 통의 열림, 화로류 점화 상태 검사 통과. 기존 사진은 0.2.0에서 검토한 원본입니다.
- 0.4.0 촬영에서 선반의 전원, 책장 점유, 큰 흘림잎의 전원 복원, 발사된 돌풍구에 의한 버튼·조명 출력, 종의 전원 상태 검사 통과.
- 사용자 지정 이름과 기존 lore 보존, 숨긴 툴팁 유지, 일반 돌 및 다른 모드 이름 공간 제외 확인.
- 일반 인벤토리·상자·크리에이티브 화면의 표시와 854×480 작은 창의 사진 축소·상세 설명·화면 경계를 스크린샷으로 검토.

## 화면 기록

- [27칸 수량표](screenshots/chest-counts-middle-ko.png)
- [큰 상자 54칸 수량표](screenshots/chest-counts-bottom-ko.png)
- [호퍼 수량표와 불가능한 출력](screenshots/hopper-counts-bottom-ko.png)
- [양조기 구성 예시](screenshots/brewing-counts-bottom-ko.png)
- [발사기의 특별 동작](screenshots/dispenser-actions-middle-ko.png)
- [선반의 사진과 설명](screenshots/shelf-details-top-ko.png)
- [작은 화면의 수량표 스크롤](screenshots/chest-counts-small-bottom-ko.png)

- [일반 인벤토리](screenshots/inventory-hover-ko.png)
- [Shift 상세 설명](screenshots/inventory-shift-ko.png)
- [자동 전환된 두 번째 사진](screenshots/inventory-slideshow-second-ko.png)
- [레드스톤 가루 설명](screenshots/redstone-dust-shift-ko.png)
- [레버 설명](screenshots/lever-shift-ko.png)
- [소리 블록: 사진과 기본·세부 정보](screenshots/note-block-shift-top-ko.png)
- [소리 블록: 악기 대응표 스크롤](screenshots/note-block-shift-middle-ko.png)
- [소리 블록: 마지막 정보까지 표시](screenshots/note-block-shift-bottom-ko.png)
- [동력 레일 전원 전달 거리](screenshots/powered-rail-details-ko.png)
- [854×480 창의 상세 설명](screenshots/inventory-small-shift-ko.png)
- [상자](screenshots/chest-hover-ko.png)
- [크리에이티브 인벤토리](screenshots/creative-hover-ko.png)
- [영어 상세 설명](screenshots/creative-shift-en.png)
- [영어 세부 정보 스크롤](screenshots/creative-details-en.png)

테스트는 프로젝트 내부의 별도 월드를 사용했습니다. 상자 화면은 테스트용 컨테이너로 생성해 표시를 검증했습니다.
사용자 게임 프로필에 설치하거나 실제 멀티플레이 서버에 접속하지 않았습니다.
다른 모드가 툴팁 화면을 대체하는 경우의 호환성 및 모든 설명 대상 블록의 모든 회로 동작을 자동 검증한 것은 아닙니다.
사진 속 회로는 촬영한 상태 예시입니다. 전환 간격은 실제 회로의 동작 시간과 다릅니다.

실행 로그에는 개발 계정의 Realms/프로필 인증, Windows 성능 카운터, 기본 그래픽 옵션 관련 경고가 있었으나
테스트를 실패시키거나 도움말 표시를 막지 않았습니다. Gradle의 향후 버전 호환 경고도 빌드를 막지 않았습니다.
