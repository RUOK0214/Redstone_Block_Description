# 검증 기록

검증일: 2026-10-31. 대상: Redstone Block Description 0.8.0, Minecraft Java 26.2,
Fabric Loader 0.19.5, Fabric API 0.160.0+26.2, Java 25, Windows.

## 결과

- `gradlew --offline build runClientGameTest`: **BUILD SUCCESSFUL**.
- 0.8.0의 `-PcaptureIllustrations -PcaptureDetails`와 `-PcaptureDetailCloseups` 촬영 성공. 44개 추가 원본을 연결했으며, 레일 경계·가마솥 전체 구조의 구도를 확인했습니다.
- 실제 H 키 입력으로 고정 후 Shift 해제·마우스 이탈에도 유지, 목차 클릭으로 악기·수량표·예시 이동, 사진 위치로 복귀를 검사했습니다.
- 일시정지 후 45틱 동안 사진 유지, 이전·다음 전환과 자동 정지, 재생 재개를 실제 버튼 클릭으로 검사했습니다.
- 고정 창 뒤의 아이템 클릭이 차단되고, Esc·닫기 버튼은 인벤토리를 유지하며 도움말만 닫는지 검사했습니다. 화면 변경 시 고정 해제도 확인했습니다.
- 1000×700 및 854×480에서 크기 변경 후 고정 유지와 버튼 범위, 한국어·영어 목차·사진 버튼을 확인했습니다.
- `gradlew --offline runClientGameTest -PcaptureIllustrations -PcaptureVibrations`: **BUILD SUCCESSFUL**. 자수정·양털·양탄자 장면 5장을 촬영하고 복사본의 SHA-256 일치를 확인했습니다. 전체 640×360 PNG는 190장입니다.
- 0.6.0의 `-PcaptureIllustrations -PcaptureCauldrons` 촬영은 다섯 상태 모두 성공했으며 해당 원본을 유지했습니다.
- 강설 상태의 빈 가마솥에 실제 게임의 강수 처리를 적용하여 가루눈 3단계가 되는지 확인하고, 눈이 내리는 화면을 촬영했습니다.
- 0.4.0에서는 `-PcaptureIllustrations -PcaptureExpanded` 실행으로 새 장면 38장을 촬영했으며, 당시 복사한 원본의 SHA-256 일치를 확인했습니다.
- 전체 233개 등록 아이템의 실제 게임 레지스트리 존재 및 기본·상세 툴팁 연결 확인. 아이템이 있는 비교기 출력 블록에 설명이 누락되지 않았는지 검사 통과.
- 한국어·영어 리소스 각각 617개 키, 양쪽 키 일치 확인.
- 한국어·영어 71개 주제에서 기능·작동 조건·수치 위주로 검토.
- 전체 설명과 양쪽 언어의 표·사진 설명을 대조했습니다. 양탄자 조사를 바로잡고 비교기 읽기와 자체 출력을 구분했습니다. 원본 사진 190장의 해상도 640×360도 모두 확인했습니다.
- 양 언어에서 모든 설명의 번역 키 해석과 최대 250 GUI 픽셀 줄바꿈 검사 통과.
- Shift 누르기 및 해제로 상세·기본 표시 전환 확인.
- 실제 마우스 휠 입력으로 긴 Shift 설명이 스크롤되는지, 끝에서 멈추는지, 다른 아이템 및 Shift 해제 후 처음으로 돌아오는지 검사 통과.
- 항목별 제목의 여백, 문단 들여쓰기와 줄 너비 검사 통과. 기능 요약 뒤의 사진은 Shift 설명에 계속 포함됩니다.
- 모든 설명 대상에서 항목 분류 후 기존 추가 정보가 빠지거나 중복되지 않는지 검사 통과.
- 한국어·영어의 모든 소제목·상단 내용 순서가 번역되고 화면 너비 안에 들어가는지 검사 통과.
- ‘수량에 따른 비교기 출력’ 제목, 한 칸의 최대 겹침 개수와 실제 낱개 수 안내, 긴 제목의 줄바꿈을 확인했습니다. 수량·음반 표는 오른쪽에서 비교기 출력을, 진동 표는 주파수를 읽습니다.
- 벌집의 꿀 수확, 정박기의 충전·사용, 독서대의 자체 신호와 비교기 읽기, 선반의 교환과 비교기 연결 방향을 맞는 소제목에 분류했습니다.
- 스크롤에 따라 상단의 현재 항목 표시가 바뀌는지, 작은 화면의 수량표에서도 표 제목과 열 이름이 고정되는지 검사 통과.
- 실제 화면에서 구분선·글머리표·문단 간격·이름 및 수치 강조·불투명한 배경·진행률 표시를 검토했습니다.
- 보관함의 기능 요약·기본 작동 뒤에 비교기 설명과 표가 나오는 순서를 확인했습니다. 통·셜커 상자·화로 및 가마솥 날씨 항목의 화면 기록을 추가했습니다.
- 동력·활성화 레일의 양방향 8칸 전원 전달과 일반 레일에 의한 단절, 20종 악기·추가 재료·25개 음높이 검사 통과.
- 감지체 반경·활성화 시간, 구리 전구 밝기, 상자 출력 수치 검사 통과. [정보 확인 기록](FACT_CHECK.md) 참고.
- 수량표의 18개 조건 조합, 총 8,037개 수량을 게임의 실제 계산과 대조했습니다. 도달할 수 없는 출력의 `—` 표시와 양조기 9개 구성 예시도 검사 통과.
- 선반의 점유 조합과 방향, 책장의 마지막 칸 번호 유지, 가마솥·케이크·정박기·동상 출력 검사 통과.
- 실제 발사기·공급기에 물 양동이를 넣어 블록 배치와 아이템 배출의 차이 검사 통과.
- 기본 음반 22종 누락 여부와 비교기 출력, 재생 중 일반 출력 15·정지 후 0, 정지 후에도 유지되는 비교기 출력, 음반 제거 후 0을 게임에서 검사했습니다.
- 주파수 1~15의 대표 이벤트 15개, 자수정 공명 태그, 양털·양탄자 32개 변형의 태그, 실제 진동 수신기의 경로 차단 차이와 설치 이벤트 억제를 검사했습니다.
- 촬영 중 자수정 공명으로 다른 감지체가 주파수 1을 수신하고 조명을 켜는지, 양털 장벽에서는 꺼지고 양탄자에서는 켜지는지 확인했습니다.
- 한국어·영어 표 머리글과 수량이 열 너비 안에 들어가는지 검사 통과. 27·54칸 상자, 호퍼, 양조기, 긴 발사기 설명, 선반·가마솥과 854×480 창을 실제 화면에서 검토.
- 보관함의 공통 비교기 문장과 덫 상자·주크박스의 기존 정보를 유지했습니다. 독서대와 퇴비통 등은 기본 용도를 추가했고, 독서대의 한쪽짜리 책 예외를 보충했습니다.
- 전체 설명 주제의 사진 파일·상태 번역·이미지 렌더러 연결, Shift를 누를 때만 사진 표시 확인.
- 1.8초 간격 슬라이드 전환, 주제 변경·마우스 이탈 후 첫 사진으로 돌아오는 동작 검사 통과.
- 두 번째 사진이 나온 상태에서 Shift를 즉시 놓았다 다시 누르면 첫 사진으로 돌아오는 실제 키 입력 검사 통과.
- 독서대의 1·2·5쪽 책에서 첫 페이지 및 마지막 페이지 출력을 검사했습니다. 한쪽짜리는 15, 두 쪽 이상이면 첫 페이지 1·마지막 페이지 15입니다.
- 일반·조율된 스컬크 감지체를 물에 잠긴 상태로 놓고 소리 블록 연주 진동에 일반 신호와 비교기 주파수 10을 계속 출력하는지 검사했습니다.
- 비교기의 비교 모드와 빼기 모드 사진이 실제 화면에서 전환되는 것을 스크린샷으로 확인.
- 새 촬영 과정에서 감지 레일·덫 상자·주크박스의 조명 출력, 통의 열림, 화로류 점화 상태 검사 통과. 기존 사진은 0.2.0에서 검토한 원본입니다.
- 0.4.0 촬영에서 선반의 전원, 책장 점유, 큰 흘림잎의 전원 복원, 발사된 돌풍구에 의한 버튼·조명 출력, 종의 전원 상태 검사 통과.
- 사용자 지정 이름과 기존 lore 보존, 숨긴 툴팁 유지, 일반 돌 및 다른 모드 이름 공간 제외 확인.
- 일반 인벤토리·상자·크리에이티브 화면의 표시와 854×480 작은 창의 사진 축소·상세 설명·화면 경계를 스크린샷으로 검토.

## 화면 기록

이번 클라이언트 검증에서 67장으로 기록했습니다.

- [고정 창의 블록 이름·목차·사진 버튼](screenshots/pinned-guide-top-ko.png)
- [목차를 눌러 악기 설명으로 이동](screenshots/pinned-guide-section-ko.png)
- [사진 다음 장을 수동으로 선택](screenshots/pinned-photo-next-ko.png)
- [작은 화면에서 고정 창 사용](screenshots/pinned-guide-small-ko.png)
- [상자 화면을 유지한 채 수량표 읽기](screenshots/pinned-container-counts-ko.png)
- [영어 고정 창과 사진 버튼](screenshots/pinned-photo-en.png)

- [상자 자체의 기능과 비교기 정보의 순서](screenshots/chest-counts-top-ko.png)
- [통의 용량과 열림 조건](screenshots/barrel-functions-top-ko.png)
- [기본 작동 아래에 구분된 비교기 정보](screenshots/barrel-functions-middle-ko.png)
- [셜커 상자의 내용물 보존](screenshots/shulker-functions-top-ko.png)
- [화로 자체의 기능](screenshots/furnace-functions-top-ko.png)
- [가마솥의 눈·비 조건과 내용물 변경](screenshots/cauldron-details-middle-ko.png)
- [한 칸의 최대 개수와 수량표 읽는 법](screenshots/chest-counts-middle-ko.png)
- [음반별 비교기 출력표](screenshots/jukebox-tracks-bottom-ko.png)
- [작은 화면의 음반 표와 고정 머리글](screenshots/jukebox-tracks-small-bottom-ko.png)
- [자수정 공명 설명](screenshots/amethyst-resonance-top-ko.png)
- [양털의 진동 차단](screenshots/wool-vibrations-bottom-ko.png)
- [양탄자의 차이](screenshots/carpet-vibrations-bottom-ko.png)
- [진동 종류별 주파수 표](screenshots/sculk-frequencies-bottom-ko.png)
- [독서대의 기본 기능과 페이지 예외](screenshots/lectern-pages-bottom-ko.png)
- [벌집의 기본 용도와 비교기 읽기](screenshots/beehive-function-top-ko.png)
- [Shift를 다시 누르면 첫 사진으로 초기화](screenshots/inventory-slideshow-restart-ko.png)
- [큰 상자 54칸 수량표](screenshots/chest-counts-bottom-ko.png)
- [호퍼 수량표와 불가능한 출력](screenshots/hopper-counts-bottom-ko.png)
- [양조기 구성 예시](screenshots/brewing-counts-bottom-ko.png)
- [발사기의 항목 분류·제목·이름 강조](screenshots/dispenser-actions-middle-ko.png)
- [선반의 사진과 설명](screenshots/shelf-details-top-ko.png)
- [작은 화면의 수량표와 고정 머리글](screenshots/chest-counts-small-bottom-ko.png)

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
