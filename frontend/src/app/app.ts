import { Component, ElementRef, OnInit, ViewChild, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class App implements OnInit {
  @ViewChild('gridCanvas') gridCanvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('sortCanvas') sortCanvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('bstCanvas') bstCanvas!: ElementRef<HTMLCanvasElement>;

  tab = signal<'path' | 'sort' | 'bst' | 'table'>('path');
  speed = signal<number>(25);
  isRunning = signal<boolean>(false);

  pathAlgo = 'dijkstra';
  rows = 22;
  cols = 35;
  grid: number[][] = [];
  start = [5, 5];
  end = [16, 28];
  isMouseDown = false;

  sortAlgo = 'quick';
  sortArray: number[] = [];
  sortColors: string[] = [];

  bstVal = 50;
  bstTraverseOrder = 'inorder';
  bstTree: { val: number; x: number; y: number; parentVal: number }[] = [];

  private ws!: WebSocket;
  private queuedSteps: any[] = [];
  private stepIdx = 0;
  private animId?: any;

  ngOnInit(): void {
    this.initGrid();
    this.resetSortArray();
    this.connectWs();
  }

  setTab(t: 'path' | 'sort' | 'bst' | 'table'): void {
    this.tab.set(t);
    setTimeout(() => {
      if (t === 'path') this.drawGrid();
      if (t === 'sort') this.drawSort();
      if (t === 'bst') this.drawBst();
    }, 50);
  }

  private connectWs(): void {
    this.ws = new WebSocket('ws://localhost:8080/ws/algo');
    this.ws.onmessage = (ev) => {
      const data = JSON.parse(ev.data);
      this.queuedSteps = data.steps || [];
      this.stepIdx = 0;
      this.playAnimation();
    };
  }

  initGrid(): void {
    this.grid = Array.from({ length: this.rows }, () => Array(this.cols).fill(0));
    setTimeout(() => this.drawGrid(), 50);
  }

  drawGrid(): void {
    if (!this.gridCanvas) return;
    const cv = this.gridCanvas.nativeElement;
    const ctx = cv.getContext('2d');
    if (!ctx) return;

    const cellW = cv.width / this.cols;
    const cellH = cv.height / this.rows;

    ctx.clearRect(0, 0, cv.width, cv.height);
    for (let r = 0; r < this.rows; r++) {
      for (let c = 0; c < this.cols; c++) {
        if (r === this.start[0] && c === this.start[1]) ctx.fillStyle = '#10b981';
        else if (r === this.end[0] && c === this.end[1]) ctx.fillStyle = '#ef4444';
        else if (this.grid[r][c] === 1) ctx.fillStyle = '#1f2937';
        else if (this.grid[r][c] === 2) ctx.fillStyle = '#93c5fd';
        else if (this.grid[r][c] === 3) ctx.fillStyle = '#f59e0b';
        else ctx.fillStyle = '#ffffff';

        ctx.fillRect(c * cellW, r * cellH, cellW - 1, cellH - 1);
        ctx.strokeStyle = '#e5e7eb';
        ctx.strokeRect(c * cellW, r * cellH, cellW, cellH);
      }
    }
  }

  onGridMouseDown(e: MouseEvent): void {
    this.isMouseDown = true;
    this.toggleWall(e);
  }

  onGridMouseMove(e: MouseEvent): void {
    if (!this.isMouseDown) return;
    this.toggleWall(e);
  }

  onGridMouseUp(): void {
    this.isMouseDown = false;
  }

  private toggleWall(e: MouseEvent): void {
    const cv = this.gridCanvas.nativeElement;
    const rect = cv.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;
    const c = Math.floor(x / (cv.width / this.cols));
    const r = Math.floor(y / (cv.height / this.rows));
    if (r < 0 || r >= this.rows || c < 0 || c >= this.cols) return;
    if ((r === this.start[0] && c === this.start[1]) || (r === this.end[0] && c === this.end[1])) return;
    this.grid[r][c] = 1;
    this.drawGrid();
  }

  clearWalls(): void {
    this.grid = this.grid.map(row => row.map(v => (v === 1 ? 0 : v)));
    this.resetPathVisuals();
  }

  resetPathVisuals(): void {
    this.grid = this.grid.map(row => row.map(v => (v > 1 ? 0 : v)));
    this.drawGrid();
  }

  runPath(): void {
    this.resetPathVisuals();
    this.isRunning.set(true);
    this.ws.send(JSON.stringify({
      domain: 'path',
      algo: this.pathAlgo,
      grid: this.grid,
      sr: this.start[0],
      sc: this.start[1],
      er: this.end[0],
      ec: this.end[1]
    }));
  }

  resetSortArray(): void {
    this.sortArray = Array.from({ length: 40 }, () => Math.floor(Math.random() * 90) + 10);
    this.sortColors = Array(this.sortArray.length).fill('#2563eb');
    setTimeout(() => this.drawSort(), 50);
  }

  drawSort(): void {
    if (!this.sortCanvas) return;
    const cv = this.sortCanvas.nativeElement;
    const ctx = cv.getContext('2d');
    if (!ctx) return;

    ctx.clearRect(0, 0, cv.width, cv.height);
    const barW = cv.width / this.sortArray.length;
    for (let i = 0; i < this.sortArray.length; i++) {
      const h = (this.sortArray[i] / 100) * cv.height;
      ctx.fillStyle = this.sortColors[i];
      ctx.fillRect(i * barW, cv.height - h, barW - 2, h);
    }
  }

  runSort(): void {
    this.isRunning.set(true);
    this.ws.send(JSON.stringify({
      domain: 'sort',
      algo: this.sortAlgo,
      array: this.sortArray
    }));
  }

  drawBst(highlightVal?: number): void {
    if (!this.bstCanvas) return;
    const cv = this.bstCanvas.nativeElement;
    const ctx = cv.getContext('2d');
    if (!ctx) return;

    ctx.clearRect(0, 0, cv.width, cv.height);
    for (const n of this.bstTree) {
      if (n.parentVal !== -1) {
        const p = this.bstTree.find(x => x.val === n.parentVal);
        if (p) {
          ctx.beginPath();
          ctx.moveTo(p.x, p.y);
          ctx.lineTo(n.x, n.y);
          ctx.strokeStyle = '#9ca3af';
          ctx.lineWidth = 2;
          ctx.stroke();
        }
      }
    }
    for (const n of this.bstTree) {
      ctx.beginPath();
      ctx.arc(n.x, n.y, 18, 0, 2 * Math.PI);
      ctx.fillStyle = n.val === highlightVal ? '#f59e0b' : '#2563eb';
      ctx.fill();
      ctx.strokeStyle = '#ffffff';
      ctx.lineWidth = 2;
      ctx.stroke();

      ctx.fillStyle = '#ffffff';
      ctx.font = '12px system-ui';
      ctx.textAlign = 'center';
      ctx.textBaseline = 'middle';
      ctx.fillText(n.val.toString(), n.x, n.y);
    }
  }

  insertBst(): void {
    if (!this.bstVal) return;
    this.isRunning.set(true);
    this.ws.send(JSON.stringify({ domain: 'bst', action: 'insert', val: this.bstVal }));
  }

  traverseBst(): void {
    this.isRunning.set(true);
    this.ws.send(JSON.stringify({ domain: 'bst', action: 'traverse', order: this.bstTraverseOrder }));
  }

  resetBst(): void {
    this.bstTree = [];
    this.ws.send(JSON.stringify({ domain: 'bst', action: 'reset' }));
    this.drawBst();
  }

  nextStep(): void {
    if (this.stepIdx >= this.queuedSteps.length) {
      this.isRunning.set(false);
      return;
    }
    const st = this.queuedSteps[this.stepIdx++];

    if (st.type === 'visit') {
      if ((st.x !== this.start[0] || st.y !== this.start[1]) && (st.x !== this.end[0] || st.y !== this.end[1])) {
        this.grid[st.x][st.y] = 2;
        this.drawGrid();
      }
    } else if (st.type === 'path') {
      if ((st.x !== this.start[0] || st.y !== this.start[1]) && (st.x !== this.end[0] || st.y !== this.end[1])) {
        this.grid[st.x][st.y] = 3;
        this.drawGrid();
      }
    } else if (st.type === 'cmp') {
      this.sortColors.fill('#2563eb');
      if (st.i >= 0) this.sortColors[st.i] = '#f59e0b';
      if (st.j >= 0) this.sortColors[st.j] = '#ef4444';
      this.drawSort();
    } else if (st.type === 'swap') {
      const t = this.sortArray[st.i];
      this.sortArray[st.i] = this.sortArray[st.j];
      this.sortArray[st.j] = t;
      this.sortColors[st.i] = '#10b981';
      this.sortColors[st.j] = '#10b981';
      this.drawSort();
    } else if (st.type === 'set') {
      this.sortArray[st.i] = st.val;
      this.sortColors[st.i] = '#10b981';
      this.drawSort();
    } else if (st.type === 'bst_visit') {
      this.drawBst(st.val);
    } else if (st.type === 'bst_add') {
      this.computeBstCoords(st.val, st.parent);
      this.drawBst(st.val);
    }
  }

  private computeBstCoords(val: number, parentVal: number): void {
    if (parentVal === -1) {
      this.bstTree.push({ val, x: 400, y: 40, parentVal: -1 });
      return;
    }
    const p = this.bstTree.find(n => n.val === parentVal);
    if (!p) return;
    const depth = Math.floor(p.y / 60) + 1;
    const spread = Math.max(180 / depth, 35);
    const x = val < p.val ? p.x - spread : p.x + spread;
    const y = p.y + 60;
    this.bstTree.push({ val, x, y, parentVal });
  }

  private playAnimation(): void {
    if (this.animId) clearTimeout(this.animId);
    const loop = () => {
      if (this.stepIdx < this.queuedSteps.length) {
        this.nextStep();
        this.animId = setTimeout(loop, this.speed());
      } else {
        this.isRunning.set(false);
        if (this.tab() === 'sort') this.sortColors.fill('#10b981');
        if (this.tab() === 'sort') this.drawSort();
      }
    };
    loop();
  }

  pauseAnimation(): void {
    if (this.animId) clearTimeout(this.animId);
    this.isRunning.set(false);
  }
}
