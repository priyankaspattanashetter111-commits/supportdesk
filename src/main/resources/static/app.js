const $=id=>document.getElementById(id);let auth='';
function message(text){$('message').textContent=text;}
async function api(path,method='GET',body){if(!auth)throw Error('Sign in first.');const r=await fetch('/api'+path,{method,headers:{Authorization:auth,...(body?{'Content-Type':'application/json'}:{})},body:body?JSON.stringify(body):undefined});const text=await r.text();let data;try{data=text?JSON.parse(text):null;}catch{data=null;}if(!r.ok)throw Error(data?.detail||('Request failed: '+r.status));return data;}
function action(id,fn){$(id).onclick=async()=>{try{await fn();message('Done.');}catch(e){message(e.message);}};}
function table(id,headers,rows){const t=document.createElement('table');const head=t.createTHead().insertRow();headers.forEach(h=>{const th=document.createElement('th');th.textContent=h;head.append(th);});const b=t.createTBody();rows.forEach(values=>{const row=b.insertRow();values.forEach(v=>{row.insertCell().textContent=v??'-';});});$(id).replaceChildren(t);}
action('login',async()=>{auth='Basic '+btoa($('username').value+':'+$('password').value);$('password').value='';await refresh();});
action('logout',async()=>{auth='';$('password').value='';document.querySelectorAll('tbody').forEach(t=>t.replaceChildren());$('events').textContent='Signed out.';if($('metrics'))$('metrics').textContent='Signed out.';});
action('refresh',refresh);

async function refresh(){const result=await api('/tickets?size=100'+($('filter').value?'&status='+$('filter').value:''));table('tickets',['ID','Title','Owner','Assignee','Priority','Status','Due UTC'],result.content.map(x=>[x.id,x.title,x.owner,x.assignee,x.priority,x.status,x.dueAt]));}
action('create',async()=>{const t=await api('/tickets','POST',{title:$('title').value,description:$('description').value,priority:$('priority').value});$('ticketId').value=t.id;await refresh();});
action('claim',async()=>{await api('/tickets/'+$('ticketId').value+'/claim','POST',{});await refresh();});
action('transition',async()=>{await api('/tickets/'+$('ticketId').value+'/status','POST',{status:$('next').value});await refresh();});
action('addComment',async()=>{await api('/tickets/'+$('ticketId').value+'/comments','POST',{text:$('comment').value});$('comment').value='';});
action('audit',async()=>{$('events').textContent=JSON.stringify(await api('/tickets/'+$('ticketId').value+'/events?size=100'),null,2);});
action('report',async()=>{$('metrics').textContent=JSON.stringify(await api('/reports/sla'),null,2);});
