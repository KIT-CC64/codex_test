'use strict';
const $ = id => document.getElementById(id);
const escapeHtml = value => String(value).replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const defaults = {
  currentLocation: '情報講義室A', outdoorTemp: 18.5,
  indoorData: {'情報講義室A':{temp:24.2,humidity:62},'図書館 2F':{temp:21,humidity:48},'学生食堂':{temp:26.5,humidity:70},'実験室3':{temp:19.5,humidity:42}},
  reviews:[{location:'情報講義室A',feel:'快適',comment:'エアコンの温度設定が丁度良く快適です！',created_at:'サンプル'},{location:'実験室3',feel:'かなり寒い',comment:'羽織りものがあると安心です。',created_at:'サンプル'}],
  outfitLogs:[],
  clothes:[{id:1,name:'ライトベージュ カーディガン',category:'中厚',temp_min:15,temp_max:22},{id:2,name:'ホワイト コットンシャツ',category:'薄手',temp_min:18,temp_max:25},{id:3,name:'やわらかスウェット パーカー',category:'中厚',temp_min:12,temp_max:19},{id:4,name:'薄手ウールジャケット',category:'厚手',temp_min:8,temp_max:16}]
};
const state = structuredClone(defaults);
function notify(text) { $('notice').textContent=text; $('notice').classList.remove('hidden'); }
try {
  const saved=JSON.parse(localStorage.getItem('climafit-v1') || 'null');
  if(saved) for(const key of ['clothes','reviews','outfitLogs']) if(Array.isArray(saved[key])) state[key]=saved[key];
} catch { notify('保存データを読み込めませんでした。初期データで表示しています。'); }
function persist() {
  try {localStorage.setItem('climafit-v1',JSON.stringify({clothes:state.clothes,reviews:state.reviews,outfitLogs:state.outfitLogs}));}
  catch {notify('端末に保存できませんでした。今回の変更はアプリを閉じると失われる場合があります。');}
}
function switchTab(tab) {
  if(!['dashboard','review','logs','reviews'].includes(tab)) return;
  document.querySelectorAll('.tab-page').forEach(el=>el.classList.toggle('hidden',el.id!==`tab-${tab}`));
  document.querySelectorAll('.nav-btn').forEach(el=>{const active=el.id===`nav-${tab}`;el.classList.toggle('text-peach-600',active);el.classList.toggle('text-warm-700',!active);el.setAttribute('aria-current',active?'page':'false');});
  $('main-content').scrollTop=0;
  if(tab==='logs') renderOutfitLogs();
  if(tab==='reviews') renderReviews();
}
function suggestion(temp,outdoor) {
  if(Math.min(temp,outdoor)<10) return {title:'コートを重ねて、室内で調節',description:'寒い場所では防寒を。暖かい室内では上着を脱いで調整しましょう。',layers:['コート / 厚手ジャケット','長袖シャツ / ニット']};
  if(Math.abs(temp-outdoor)>=5) return {title:'脱ぎ着しやすいカーディガン重ね着',description:'室内と屋外の寒暖差に備え、着脱しやすい羽織りものを持ちましょう。',layers:['カーディガン / 薄手ジャケット',temp>=25?'半袖Tシャツ':'長袖シャツ / 長袖Tシャツ']};
  if(temp>=25) return {title:'通気性の良い薄手の服装',description:'暑さを感じやすい温度です。薄手の素材で快適に過ごしましょう。',layers:['半袖Tシャツ / 通気性の良いシャツ']};
  if(temp<20) return {title:'長袖に羽織りものをプラス',description:'肌寒さに備えて薄手のジャケットを重ねましょう。',layers:['薄手ジャケット / パーカー','長袖シャツ']};
  return {title:'軽やかな長袖スタイル',description:'長袖シャツを基本に、体感に合わせて調節しましょう。',layers:['長袖シャツ / 薄手のトップス']};
}
function updateLocationData() {
  state.currentLocation=$('location-select').value;
  const d=state.indoorData[state.currentLocation];
  const di=0.81*d.temp+0.01*d.humidity*(0.99*d.temp-14.3)+46.3;
  const status=di<60?'寒い':di<65?'肌寒い':di<75?'快適':di<80?'やや暑い':'暑い';
  $('indoor-temp').textContent=d.temp.toFixed(1); $('indoor-humidity').textContent=`${d.humidity}%`;
  $('discomfort-badge').textContent=`${status} (${di.toFixed(0)})`;
  $('discomfort-val').textContent=`${di.toFixed(1)} (${status})`;
  $('discomfort-desc').textContent=`温湿度から算出した目安です。現在の判定：${status}。`;
  const s=suggestion(d.temp,state.outdoorTemp);
  $('recommend-context').textContent=`外気${state.outdoorTemp}℃ × 室内${d.temp}℃`;
  $('recommend-title').textContent=s.title; $('recommend-description').textContent=s.description;
  $('layer-guide').innerHTML=s.layers.map((x,i)=>`<div class="bg-white rounded-xl p-3 text-xs border border-warm-200"><strong>${i+1}. ${escapeHtml(x)}</strong><p class="mt-1 text-warm-700">${i===0&&s.layers.length>1?'寒い場所で着用し、暖かい場所では脱いで調節':'体感に合わせて調節してください'}</p></div>`).join('');
}
function refreshData(){updateLocationData();notify('デモデータを再表示しました。センサー・天気APIには未接続です。');}
function filterTemperature(prefix) {
  const mode=$(`${prefix}-mode`).value;
  $(`${prefix}-temp`).disabled=mode!=='custom';
  const raw=$(`${prefix}-temp`).value;
  const value=mode==='current'?state.indoorData[state.currentLocation].temp:Number(raw);
  const valid=mode==='all'||(raw!==''&&Number.isFinite(value)&&value>=-50&&value<=60);
  $(`${prefix}-summary`).textContent=!valid?'−50〜60℃の温度を入力してください。':mode==='all'?'すべて表示':`${value}℃${prefix==='logs'?'（記録時の室温 ±2℃）':'が適温範囲に入る服'}`;
  return {mode,value,valid};
}
function saveSuggestion(){
 const d=state.indoorData[state.currentLocation],s=suggestion(d.temp,state.outdoorTemp);
 state.outfitLogs.unshift({id:Date.now(),date:new Date().toISOString(),location:state.currentLocation,indoor_temp:d.temp,weather_temp:state.outdoorTemp,title:s.title,comment:s.description});persist();switchTab('logs');
}
function renderOutfitLogs(){
 const f=filterTemperature('logs');const items=state.outfitLogs.filter(x=>f.valid&&(f.mode==='all'||Math.abs(x.indoor_temp-f.value)<=2));
 $('log-count').textContent=`${items.length} / ${state.outfitLogs.length}`;
 $('logs-container').innerHTML=items.map(x=>`<article class="bg-white border border-warm-200 rounded-2xl p-3 space-y-2"><p class="text-[10px] text-warm-700">${escapeHtml(new Date(x.date).toLocaleString('ja-JP'))} · ${escapeHtml(x.location)}</p><h3 class="text-xs font-bold">${escapeHtml(x.title)}</h3><p class="text-xs">室内 ${x.indoor_temp}℃ / 外気 ${x.weather_temp}℃</p><p class="text-xs text-warm-700">${escapeHtml(x.comment)}</p></article>`).join('')||'<p class="text-xs text-warm-700 py-8 text-center">条件に合う記録はありません。提案画面から保存できます。</p>';
}
function postReview(){
 const comment=$('review-comment').value.trim();if(!comment||comment.length>500){notify('口コミを1〜500文字で入力してください。');return;}
 state.reviews.unshift({location:$('review-location').value,feel:$('review-feel').value,comment,created_at:new Date().toLocaleString('ja-JP')});persist();$('review-comment').value='';renderReviews();notify('口コミを端末に保存しました。他の利用者には送信されません。');
}
function renderReviews(){ $('reviews-list').innerHTML=state.reviews.map(x=>`<article class="bg-white border border-warm-200 rounded-xl p-3 space-y-2"><div class="flex justify-between gap-2"><strong class="text-xs">${escapeHtml(x.location)}</strong><span class="text-xs bg-sage-100 rounded-full px-2">${escapeHtml(x.feel)}</span></div><p class="text-xs break-words">${escapeHtml(x.comment)}</p><p class="text-[10px] text-warm-700">${escapeHtml(x.created_at)}</p></article>`).join(''); }
// Backend adapters may call this after fetching real sensor/weather data.
window.ClimaFit={setClimate(payload){
 if(!payload||!Number.isFinite(payload.outdoorTemp)||!payload.indoorData)throw new Error('Invalid climate payload');
 for(const name of Object.keys(defaults.indoorData)){const d=payload.indoorData[name];if(!d||!Number.isFinite(d.temp)||!Number.isFinite(d.humidity)||d.humidity<0||d.humidity>100)throw new Error('Invalid room data');}
 state.outdoorTemp=payload.outdoorTemp;state.indoorData=structuredClone(payload.indoorData);
 // Outdoor card is synchronized with the same payload.
 const outdoor=$('tab-dashboard').querySelectorAll('.text-3xl')[1];outdoor.textContent=state.outdoorTemp.toFixed(1);
 updateLocationData();renderOutfitLogs();
}};
updateLocationData(); switchTab('dashboard');

let selectedPhoto = null;
let photoReadVersion = 0;
function handleImageSelect(event) {
  const version=++photoReadVersion;
  selectedPhoto=null; $('analyze-btn').disabled=true;
  $('image-preview-container').classList.add('hidden'); $('upload-placeholder').classList.remove('hidden');
  const file=event.target.files[0]; if(!file)return;
  if(!file.type.startsWith('image/')||file.size>10*1024*1024){notify('10MB以下の画像を選んでください。');event.target.value='';return;}
  const reader=new FileReader();
  reader.onload=()=>{if(version!==photoReadVersion)return;selectedPhoto=reader.result;$('image-preview').src=selectedPhoto;$('image-preview-container').classList.remove('hidden');$('upload-placeholder').classList.add('hidden');$('analyze-btn').disabled=false;};
  reader.onerror=()=>notify('画像を読み込めませんでした。'); reader.readAsDataURL(file);
}
function resetImageSelect(event){event.preventDefault();event.stopPropagation();++photoReadVersion;selectedPhoto=null;$('camera-input').value='';$('image-preview').removeAttribute('src');$('image-preview-container').classList.add('hidden');$('upload-placeholder').classList.remove('hidden');$('analyze-btn').disabled=true;$('ai-result-card').classList.add('hidden');}
function runAIAnalysis(){if(!selectedPhoto)return;notify('AI診断はバックエンド接続後に利用できます。写真は送信していません。');}
function saveOutfitLog(){notify('診断結果はまだありません。バックエンドの接続をお待ちください。');}
